package com.alanwgt.helpers;

import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.model.Transacao;
import com.alanwgt.App;
import com.alanwgt.console.Logger;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import javax.annotation.Nullable;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PilaCoinManager {

    private static final BigInteger bigO = new BigInteger("99999998000000000000000000000000000000000000000000000000000000000000000");
    // volatile: makes the compiler use the L3 cache (L3 cache, differently from L1 and L2 that belongs to each core, is shared across all of the cores)
    private static boolean mine = false;
    private static AtomicLong hashes = new AtomicLong(0L);

    private static List<Thread> miners = new ArrayList<>();
    private static PilaStore pilaStore;

    public static PublishSubject<PilaCoin> pilaFound = PublishSubject.create();
    private static PublishSubject<Long> hashResetSubject = PublishSubject.create();

    public static Observable<PilaCoin> onPilaFound() {
        return pilaFound;
    }

    public static Observable<Long> onHashReset() {
        return hashResetSubject;
    }

    public static PilaStore getPilaStore() {
        return pilaStore;
    }

    public static boolean isMining() {
        return mine;
    }

    public static void loadPilaStore() throws Exception {
        pilaStore = PilaStore.loadFromStorage();
    }

    public static void start(PilaCoin pilaCoin) {
        mine = true;
        spawnThreads(
                Runtime.getRuntime().availableProcessors(),
                () -> {
                    byte[] serialized;

                    try {
                        serialized = ObjectUtil.serialize(pilaCoin);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    PilaCoin mPila;

                    try {
                        mPila = (PilaCoin) ObjectUtil.deserialize(serialized);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }

                    SecureRandom sr = new SecureRandom();
                    BigInteger bi;
                    byte[] hash;

                    while (mine) {
                        Long magicNumber = sr.nextLong();
                        mPila.setDataCriacao(new Date());

                        try {
                            mPila.setNumeroMagico(magicNumber);
                            hash = ObjectUtil.hash(mPila);
                            hashes.addAndGet(1L);
                        } catch (NoSuchAlgorithmException | IOException e) {
                            e.printStackTrace();
                            continue;
                        }

                        bi = new BigInteger(
                                1,
                                hash
                        );

                        if (bi.compareTo(bigO) < 0) {
                            pilaFound.onNext(mPila);
                        }
                    }
                }
        );

        new Thread(PilaCoinManager::hashCountResetter).start();
    }

    private static void hashCountResetter() {
        while (mine) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hashResetSubject.onNext(hashes.get());
            hashes.set(0L);
        }
    }

    public static Long getHashCount() {
        return hashes.get();
    }

    // TODO check if this is actually working
    public static void doTransfer(PilaCoin pilaCoin, String newOwnerId) throws Exception {
        Transacao transacao = new Transacao();
        transacao.setDataTransacao(new Date());
        transacao.setAssinaturaDono(null);
        transacao.setIdNovoDono(newOwnerId);

        byte[] signature = KeyManager.getPrivateCipher().encrypt(
                ObjectUtil.hash(transacao)
        ).getData();

        transacao.setAssinaturaDono(signature);

        List<Transacao> transactions = pilaCoin.getTransacoes();
        transactions.add(transacao);

        pilaCoin.setTransacoes(transactions);

        Mensagem message = new Mensagem();
        message.setMaster(false);
        message.setTipo(Mensagem.TipoMensagem.PILA_TRANSF);
        message.setPilaCoin(pilaCoin);

        signature = KeyManager.getPrivateCipher().encrypt(
                ObjectUtil.hash(pilaCoin)
        ).getData();

        message.setAssinatura(signature);
        message.setChavePublica((PublicKey) KeyManager.getPublicKey());
        message.setIdOrigem(App.MY_ID);
//        message.set
        // TODO: something is missing here. There is no place to put the pila coin

        Master.sendPacket(
                ObjectUtil.serialize(message)
        );
    }

    public static void stopMining(@Nullable Runnable runnable) {
        mine = false;

        new Thread(() -> {
            for (Thread t : miners) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (runnable != null) {
                runnable.run();
            }
        }).start();
    }

    private static void spawnThreads(int n, Runnable runnable) {
        n = n-1;
        Logger.info("mining started with " + n + " thread(s)");
        for (int i = 0; i < n; i++) {
            Thread t = new Thread(runnable);
            miners.add(t);
            t.start();
        }
    }

    public static class PilaStore implements Serializable {

        private static final long serialVersionUID = 1113799434508676095L;
        public static final Path path = Paths.get(System.getProperty("user.dir"), "pila_coins");

        private List<PilaCoin> pilas = new ArrayList<>();

        private PilaStore() {}

        public void addPila(PilaCoin pilaCoin) throws Exception {
            pilas.add(pilaCoin);
            save();
        }

        public int getPilaSize() {
            return pilas.size();
        }

        public void save() throws Exception {
            FileOutputStream fos = new FileOutputStream(path.toFile());
            byte[] serialized = ObjectUtil.serialize(this);

            if (serialized.length == 0) {
                Logger.error("EMPTY SERIALIZED PILA STORE! Something went wrong...");
                return;
            }

            fos.write(serialized);
            fos.close();
            Logger.info("pilas saved to store");
        }

        public static PilaStore loadFromStorage() throws Exception {
            File file = new File(path.toString());

            if (!file.isFile()) {
                if (!file.createNewFile()) {
                    Logger.error("could not create pila store!");
                    return null;
                }
                Logger.info("created file to store pilas at root");
                PilaStore ps = new PilaStore();
                ps.save();
                return ps;
            }

            Logger.info("loading PilaStore from storage: " + path);
            byte[] fileBytes = Files.readAllBytes(path);

            return (PilaStore) ObjectUtil.deserialize(fileBytes);
        }

    }
}

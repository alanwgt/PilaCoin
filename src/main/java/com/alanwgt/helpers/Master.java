package com.alanwgt.helpers;

import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.model.ObjetoTroca;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.model.Transacao;
import com.alanwgt.App;
import com.alanwgt.security.*;
import com.alanwgt.console.Logger;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class Master {

    private volatile static String tcpHost = App.MASTER_BROADCAST_HOST;
    private volatile static int tcpPort;
    private volatile static boolean watchingMaster = false, listening = false;
//    private static final int DATAGRAM_PACKET_PORT = 3333;
    private static final int DATAGRAM_PACKET_PORT = 3332;
    private static final Gson gson = new Gson();

    private static final PublishSubject<Exception> subject = PublishSubject.create();
    private static final PublishSubject<PilaCoin> pilaCoinValidatedSubject = PublishSubject.create();
    private static final PublishSubject<Mensagem> messageReceivedSubject = PublishSubject.create();

    public static Observable<Exception> onWatchogError() {
        return subject;
    }
    public static Observable<PilaCoin> onPilaCoinValidated() {
        return pilaCoinValidatedSubject;
    }
    public static Observable<Mensagem> onMessageReceived() {
        return messageReceivedSubject;
    }

    private static Map<String, User> users = new HashMap<>();

    public static String getTcpHost() {
        return tcpHost;
    }

    public static int getTcpPort() {
        return tcpPort;
    }

    public static Map<String, User> getUsers() {
        return users;
    }

    public static void stopWatchdog() {
        watchingMaster = false;
    }

    public static void initWatchdog() {
        if (watchingMaster) {
            Logger.warning("watchdog already started!");
            return;
        }

        watchingMaster = true;

        new Thread(() -> {
           while(watchingMaster) {
               try {
                   discover();
                   Thread.sleep(15000);
               } catch (IOException | InterruptedException e) {
                   subject.onNext(e);
               }
           }
        }).start();
    }

    public static PilaCoin validatePilaCoin(PilaCoin pilaCoin) throws Exception {
        Logger.debug(String.format("trying to open a TCP socket on %s:%d", tcpHost, tcpPort));

        if (tcpPort == 0) {
            Logger.warning("the TCP port was not defined! attempting on port 4444");
            tcpPort = 4444;
        }

        Socket socket = new Socket(tcpHost, tcpPort);
        SecretKey secretKey = KeyMaster.generateSecretKey(128);
        // encrypting the secret key with master's public key
        Cipher.Encrypted encryptedSecretKey = KeyManager.getMasterCipher().encrypt(secretKey.getEncoded());
        AESCipher secretCipher = new AESCipher(secretKey);
        secretCipher.init();

        // serialize the pila and encrypt it with the secret key
        byte[] bPila = ObjectUtil.serialize(pilaCoin);

        // encrypt it with the secret key
        byte[] encryptedPila = secretCipher.encrypt(
                bPila
        ).getData();

        // do signature
        byte[] signature = KeyManager.getPrivateCipher().encrypt(
                ObjectUtil.hash(bPila)
        ).getData();

        // put the encrypted secret key and serialized pila in an ObjectoTroca
        ObjetoTroca objetoTroca = new ObjetoTroca();
        objetoTroca.setChaveSessao(encryptedSecretKey.getData());
        objetoTroca.setChavePublica((PublicKey) KeyManager.getPublicKey());
        objetoTroca.setAssinatura(signature);
        objetoTroca.setObjetoSerializadoCriptografado(encryptedPila);

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(objetoTroca);

        Logger.debug("pila sent to validation");

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Object o = ois.readObject();

        Logger.debug("pila received back from server. checking...");

        if (o instanceof ObjetoTroca) {
            ObjetoTroca ot = (ObjetoTroca) o;

            if (!Arrays.equals(
                    ObjectUtil.hash(
                            secretCipher.decrypt(
                                    new Cipher.Encrypted(
                                            ot.getObjetoSerializadoCriptografado(),
                                            null
                                    ))),
                    KeyManager.getMasterCipher().decrypt(
                            new Cipher.Encrypted(ot.getAssinatura(), null)
                    )
            )) {
                Logger.error("PILA SIGNATURE DOESN'T MATCH!!");
                Logger.error("discarding pila...");
                return null;
            }

            Logger.debug("pila signature verified!");

            PilaCoin pc = (PilaCoin) ObjectUtil.deserialize(
                    secretCipher.decrypt(
                        new Cipher.Encrypted(ot.getObjetoSerializadoCriptografado(), null))
            );

            pilaCoinValidatedSubject.onNext(pc);
            return pc;

        } else if (o instanceof Mensagem) {
            Mensagem m = (Mensagem) o;
            Logger.warning("received a message from master");
            if (m.getErro() != null) {
                Logger.error(m.getErro());
            }
        } else {
            Logger.error("received an unknown object from master");
        }

        return null;
    }

    public static void sendPacket(byte[] data) throws IOException {

        DatagramSocket datagramSocket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(
                data,
                0,
                data.length,
                InetAddress.getByName(App.MASTER_BROADCAST_HOST),
                App.MASTER_BROADCAST_PORT
        );

        datagramSocket.send(packet);
        datagramSocket.close();
    }

    private static void discover() throws IOException {
        Mensagem message = new Mensagem();
        message.setIdOrigem(App.MY_ID);
        message.setTipo(Mensagem.TipoMensagem.DISCOVER);
        message.setChavePublica((PublicKey) KeyManager.getPublicKey());
        message.setMaster(false);
        message.setPorta(DATAGRAM_PACKET_PORT);

        byte[] data = ObjectUtil.serialize(message);

        Logger.debug(String.format("sending signal to master on %s:%d", App.MASTER_BROADCAST_HOST, App.MASTER_BROADCAST_PORT));

        sendPacket(data);
    }

    public static void listen() {
        if (listening) {
            Logger.warning("already listening master!");
            return;
        }

        new Thread(() -> {

            DatagramSocket datagramSocket;

            try {
                datagramSocket = new DatagramSocket(DATAGRAM_PACKET_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
                return;
            }

            listening = true;
            byte[] buffer = new byte[2048];
            DatagramPacket packet;
            Mensagem message;


            while (listening && watchingMaster) {
                packet = new DatagramPacket(buffer, buffer.length);
                try {
//                    datagramSocket.setSoTimeout(5000);
                    datagramSocket.receive(packet);
                } catch (Exception e) {
                    Logger.error("An error occurred while listening for a message ...");
                    messageReceivedSubject.onError(e);
                    try {
                        datagramSocket.close();
                    } catch (Exception ignored) {}
                    continue;
                }

                Logger.debug("packet received.");

                try {
                    message = (Mensagem) ObjectUtil.deserialize(packet);
                } catch (ClassNotFoundException | IOException e) {
                    Logger.warning("something went wrong with the deserialization; this probably means that someone sent a wrong message");
                    Logger.warning("packet from: " + packet.getAddress() + ":" + packet.getPort());
//                    messageReceivedSubject.onError(e);
                    continue;
                }
                Logger.debug("message of type: " + message.getTipo().toString());

                messageReceivedSubject.onNext(message);
            }
        }).start();
    }

    public static void messageHandler(Mensagem message) {

        switch (message.getTipo()) {
            case DISCOVER_RESP:
                handleDiscoverResponseMessage(message);
                break;
            case ERRO:
                Logger.error("received an error message from master.");
                Logger.log(message);
                onMessageError(new Exception(message.getErro()));
                break;
            case DISCOVER:
                handleDiscoverMessage(message);
                break;
            case PILA_TRANSF:
                handlePilaTransfer(message);
                break;
            default:
                onMessageError(new Exception("Unknown message received from master"));
        }
    }

    private static void handlePilaTransfer(Mensagem message) {
        PilaCoin tPc = message.getPilaCoin();
        Logger.info("received pila coin transfer from: " + message.getIdOrigem());

        if (!tPc.getTransacoes().get(tPc.getTransacoes().size() - 1).getIdNovoDono().equals(App.MY_ID)) {
            Logger.warning("this transaction is destined to someone else.");
            return;
        }

        // check the user public key with the one already registered
        if (!users.get(message.getIdOrigem()).getPublicKey().equals(message.getChavePublica())) {
            Logger.error("the public key from the message doesn't match with the one previously stored!");
            return;
        }

        try {
            RSACipher cipher = new RSACipher(message.getChavePublica());
            cipher.init();

            byte[] sign = ObjectUtil.hash(message);

            if (!Arrays.equals(
                    sign,
                    cipher.decrypt(new Cipher.Encrypted(message.getAssinatura(), null))
            )) {
                Logger.error("the signature doesn't match!");
                Logger.log(message);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // check master's signature
        try {
            byte[] sign = ObjectUtil.hash(tPc);
            if (!Arrays.equals(
                    sign,
                    KeyManager.getMasterCipher().decrypt(
                            new Cipher.Encrypted(tPc.getAssinaturaMaster(), null)))) {
                Logger.error("the PilaCoin was not signed by the master!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Logger.info("the pila was/is:");
        Logger.info("\t=> mined by: " + tPc.getIdCriador());
        Logger.info("\t=> identified by: " + tPc.getId());
        Logger.info("\t=> created in: " + tPc.getDataCriacao().toString());
        Logger.info("\t=> transferred: " + tPc.getTransacoes().size() + " times");

        PilaCoinManager.pilaFound.onNext(tPc);
    }

    private static void handleDiscoverMessage(Mensagem message) {
        String uid = message.getIdOrigem();

        if (users.containsKey(uid)) {
            // user already registered
            return;
        }

        User user = new User.FromMessage(message).build();
        Logger.info("discovered new user: " + user.getId());
        SocketIOInterface ioInterface = App.getIoInterface();

        if (ioInterface != null) {
            try {
                ioInterface.emit("user-discovered", gson.toJson(user));
            } catch (SocketNotConnectedException e) {
                System.err.println("socket not connected!");
            }
        }

        users.put(uid, user);
    }

    private static void handleDiscoverResponseMessage(Mensagem message) {
        if (!message.isMaster()) {
            // TODO: THE MESSAGE WAS SENT BY ANOTHER PERSON
            Logger.error("MESSAGE SENT BY ANOTHER PERSON!");
            Logger.log(message);
            return;
        }

        try {
            byte[] signature = message.getAssinatura();
            message.setAssinatura(null);

            byte[] signatureToCheck = ObjectUtil.hash(message);

            byte[] decryptedSignature = KeyManager.getMasterCipher().decrypt(
                    new Cipher.Encrypted(signature, null));

            if (!Arrays.equals(signatureToCheck, decryptedSignature)) {
                Logger.error("THE SIGNATURE DOESN'T MATCH!!");
                Logger.log(message);
                return;
            }

            Logger.debug("signature verified!");
        } catch (Exception e) {
            e.printStackTrace();
            return;
            // TODO: WRONG SIGNATURE!
        }
//        tcpHost = message.getEndereco().toString();
        tcpPort = message.getPorta();
    }

    public static void onMessageError(Throwable t) {
        Logger.error(t.toString());
    }

}

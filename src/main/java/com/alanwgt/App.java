package com.alanwgt;

import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import com.alanwgt.console.CommandHandler;
import com.alanwgt.console.Logger;
import com.alanwgt.console.Terminator;
import com.alanwgt.helpers.*;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.IO;

import java.net.URISyntaxException;
import java.security.PublicKey;

public class App {

    public static final String MY_ID = "meet@alanwgt.com";
    public static final String MASTER_BROADCAST_HOST = "127.0.0.1";
    public static final int MASTER_BROADCAST_PORT = 3333;
    private static final Terminator term = new Terminator();
    private static final CompositeDisposable disposables = new CompositeDisposable();

    private static SocketIOInterface ioInterface;

    public static void main(String... args) {
        Logger.DEBUG = false;
        // TODO: implement all the network related stuff as observables
        // TODO: the terminator must specify when a parameter is needed

        init();
//        initInterfaceConnection();
//        initCommandHandler();
    }

    public static SocketIOInterface getIoInterface() {
        return ioInterface;
    }

    private static void initInterfaceConnection() {
        IO.Options ioOptions = new IO.Options();
        ioOptions.forceNew = false;
        ioOptions.reconnection = true;
        ioOptions.port = 3010;

        try {
            ioInterface = new SocketIOInterface(
                    IO.socket("http://127.0.0.1:3010", ioOptions)
            );
        } catch (URISyntaxException e) {
            Logger.error("Couldn't connect to SocketIO Interface!");
            e.printStackTrace();
        }

        ioInterface.connect();
        Logger.setIoInterface(ioInterface);
    }

    private static void initCommandHandler() {
        CommandHandler cm = new CommandHandler(term);
        disposables.add(cm.start());
    }

    private static void init() {
        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(App::shutdown));

        try {
            PilaCoinManager.loadPilaStore();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // KeyManager manages all the available keys and ciphers
        KeyManager.init();
        // handler to when we find a pila
        disposables.add(PilaCoinManager.onPilaFound().subscribeOn(Schedulers.trampoline()).subscribe(App::onPilaFound));
        // Watchdog does the master discover and signature validation.
        Master.initWatchdog();
        // listen for incoming messages
        Master.listen();
        // handler to when an error occurs on server discover process
        disposables.add(Master.onWatchogError().subscribe(App::onWatchdogError));
        // master's message handler
        disposables.add(Master.onMessageReceived().subscribe(
                Master::messageHandler,
                Master::onMessageError)
        );

        PilaCoin basePila = generateBasePila();
        // start mining!
        PilaCoinManager.start(basePila);
    }

    public static CompositeDisposable getDisposables() {
        return disposables;
    }

    public static Terminator getTerm() {
        return term;
    }

    private static void shutdown() {
        Logger.error("server is going to shutdown now...");
        Master.stopWatchdog();
        PilaCoinManager.stopMining(() -> {
            try {
                PilaCoinManager.getPilaStore().save();
            } catch (Exception e) {
                Logger.error("couldn't save the mined pilas! D:");
            }
            disposables.dispose();
        });
    }

    public static PilaCoin generateBasePila() {
        PilaCoin basePila = new PilaCoin();
        basePila.setIdCriador(MY_ID);
        basePila.setChaveCriador((PublicKey) KeyManager.getPublicKey());
        return basePila;
    }

    public static void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    private static void onWatchdogError(Exception e) {
        Logger.error("an error has occurred with master. Shutting down the watchdog...");
        e.printStackTrace();
        Master.stopWatchdog();
    }

    private static void onPilaFound(PilaCoin pilaCoin) {
        // we need to validate the pila before saving it
        try {
            Logger.info("found a pila! Validating...");
            pilaCoin = Master.validatePilaCoin(pilaCoin);

            if (pilaCoin == null) {
                Logger.warning("something went wrong with pila validation!");
                return;
            }

            Logger.info("pila is valid!");
            PilaCoinManager.getPilaStore().addPila(pilaCoin);

        } catch (Exception e) {
            Logger.error("there was a problem while trying to validate or save pila");
            e.printStackTrace();
        }
    }

}

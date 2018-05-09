package com.alanwgt.helpers;

import com.alanwgt.App;
import com.alanwgt.console.Logger;
import com.alanwgt.console.Terminator;
import com.google.gson.Gson;
import io.socket.client.Ack;
import io.socket.client.Socket;

import javax.annotation.Nullable;

public class SocketIOInterface {

    private Gson gson = new Gson();
    private Socket socket;
    private boolean connected = false;

    public SocketIOInterface(Socket socket) {
        this.socket = socket;
    }

    public boolean isConnected() {
        return connected;
    }

    public void connect() {

        socket.on(Socket.EVENT_CONNECT, args -> {
            Logger.success("Successfully connected to SocketIO Interface!");
            try {
                emit("pilas", Integer.toString(PilaCoinManager.getPilaStore().getPilaSize()));
            } catch (SocketNotConnectedException e) {
                e.printStackTrace();
            }
        }).on(Socket.EVENT_DISCONNECT, __ -> {
            Logger.error("The socket was disconnected from SocketIO Interface!");
        }).on("command", args-> {
            Logger.success("received command: " + args[0]);
        }).on("power-off", args ->  {
            Logger.warning("received shutdown signal");
            PilaCoinManager.stopMining(() -> {
                socket.emit("power-off-success");
                Logger.warning("the miner has stopped!");
            });
        }).on("power-on", args -> {
            PilaCoinManager.start(App.generateBasePila());
            socket.emit("power-on-success");
            Logger.success("the miner has started!");
        });

        socket.connect();
        connected = true;

        App.addDisposable(PilaCoinManager.onHashReset().subscribe(l -> emit("hps", l.toString())));
        App.addDisposable(Master.onPilaCoinValidated().subscribe(pilaCoin -> emit("pila-found", gson.toJson(pilaCoin))));

        // TODO!
        if (App.getTerm() != null) {
//            Logger.info("sending term commands to socket");
//            App.addDisposable(App.getTerm().linesFromInput().subscribe(l -> emit("command", l.toString())));
        }
    }

    public void emit(String event, @Nullable Object[] args, @Nullable Ack ack) throws SocketNotConnectedException {
        if (!connected) {
            throw new SocketNotConnectedException();
        }

        if (ack == null) {
            socket.emit(event, args);
            return;
        }

        socket.emit(event, args, ack);
    }

    public void emit(String event, String message) throws SocketNotConnectedException {
        if (!connected) {
            throw new SocketNotConnectedException();
        }

        socket.emit(event, message, null);
    }

}

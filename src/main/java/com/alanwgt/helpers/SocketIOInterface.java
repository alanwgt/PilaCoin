package com.alanwgt.helpers;

import com.alanwgt.App;
import com.alanwgt.console.Logger;
import com.google.gson.Gson;
import io.socket.client.Ack;
import io.socket.client.Socket;

import javax.annotation.Nullable;
import java.util.Arrays;

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

    public Socket getSocket() {
        return socket;
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

        if (App.getCommandHandler() != null) {
            socket.on("command", c -> {
                String[] command = ((String) c[0]).split("\\s");

                if (command.length == 0) {
                    return;
                }

                App.getCommandHandler().handle(command);
            });
        }

        socket.connect();
        connected = true;

        App.addDisposable(PilaCoinManager.onHashReset().subscribe(l -> emit("hps", l.toString())));
        App.addDisposable(Master.onPilaCoinValidated().subscribe(pilaCoin -> emit("pila-found", gson.toJson(pilaCoin))));
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

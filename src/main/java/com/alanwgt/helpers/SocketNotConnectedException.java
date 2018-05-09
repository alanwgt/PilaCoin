package com.alanwgt.helpers;

public class SocketNotConnectedException extends Exception {

    public SocketNotConnectedException() {
        super("The connect() method should be called before emitting a message.");
    }

}

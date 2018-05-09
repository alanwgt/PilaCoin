package com.alanwgt.helpers;

import br.ufsm.csi.seguranca.pila.model.Mensagem;

import java.net.InetAddress;
import java.security.PublicKey;

public class User {

    private final String id;
    private final PublicKey publicKey;
    private final InetAddress inetAddress;
    private final int port;

    public User(String id, PublicKey publicKey, InetAddress inetAddress, int port) {
        this.id = id;
        this.publicKey = publicKey;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    public static class FromMessage {

        private String id;
        private PublicKey publicKey;
        private InetAddress address;
        private int port;

        public FromMessage(Mensagem message) {
            id = message.getIdOrigem();
            publicKey = message.getChavePublica();
            address = message.getEndereco();
            port = message.getPorta();
        }

        public User build() {
            return new User(
                    id,
                    publicKey,
                    address,
                    port
            );
        }

    }
}

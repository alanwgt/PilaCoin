//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package br.ufsm.csi.seguranca.server.model;

import java.net.InetAddress;
import java.security.PublicKey;

public class Usuario {
    private String id;
    private PublicKey chavePublica;
    private InetAddress endereco;

    public Usuario() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKey getChavePublica() {
        return this.chavePublica;
    }

    public void setChavePublica(PublicKey chavePublica) {
        this.chavePublica = chavePublica;
    }

    public InetAddress getEndereco() {
        return this.endereco;
    }

    public void setEndereco(InetAddress endereco) {
        this.endereco = endereco;
    }
}

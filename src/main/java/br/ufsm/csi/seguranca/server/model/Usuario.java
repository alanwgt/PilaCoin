package br.ufsm.csi.seguranca.server.model;

import java.net.InetAddress;
import java.security.PublicKey;

/**
 * Created by cpol on 19/04/2018.
 */
public class Usuario {

    private String id;
    private PublicKey chavePublica;
    private InetAddress endereco;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKey getChavePublica() {
        return chavePublica;
    }

    public void setChavePublica(PublicKey chavePublica) {
        this.chavePublica = chavePublica;
    }

    public InetAddress getEndereco() {
        return endereco;
    }

    public void setEndereco(InetAddress endereco) {
        this.endereco = endereco;
    }
}

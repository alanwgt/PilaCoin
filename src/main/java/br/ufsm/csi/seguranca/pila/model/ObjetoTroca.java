package br.ufsm.csi.seguranca.pila.model;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Created by cpol on 17/04/2018.
 */
public class ObjetoTroca implements Serializable {

    private static final long serialVersionUID = 1L;

    private PublicKey chavePublica;
    private byte[] chaveSessao;
    private byte[] objetoSerializadoCriptografado;
    private byte[] assinatura;

    public PublicKey getChavePublica() {
        return chavePublica;
    }

    public void setChavePublica(PublicKey chavePublica) {
        this.chavePublica = chavePublica;
    }

    public byte[] getChaveSessao() {
        return chaveSessao;
    }

    public void setChaveSessao(byte[] chaveSessao) {
        this.chaveSessao = chaveSessao;
    }

    public byte[] getObjetoSerializadoCriptografado() {
        return objetoSerializadoCriptografado;
    }

    public void setObjetoSerializadoCriptografado(byte[] objetoSerializadoCriptografado) {
        this.objetoSerializadoCriptografado = objetoSerializadoCriptografado;
    }

    public byte[] getAssinatura() {
        return assinatura;
    }

    public void setAssinatura(byte[] assinatura) {
        this.assinatura = assinatura;
    }
}

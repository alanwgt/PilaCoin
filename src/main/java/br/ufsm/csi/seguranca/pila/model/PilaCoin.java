package br.ufsm.csi.seguranca.pila.model;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

/**
 * Created by cpol on 12/04/2018.
 */
public class PilaCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idCriador;
    private Date dataCriacao;
    private PublicKey chaveCriador;
    private Long numeroMagico;
    private byte[] assinaturaMaster;
    private Long id;
    private List<Transacao> transacoes;

    public List<Transacao> getTransacoes() {
        return transacoes;
    }

    public void setTransacoes(List<Transacao> transacoes) {
        this.transacoes = transacoes;
    }

    public String getIdCriador() {
        return idCriador;
    }

    public void setIdCriador(String idCriador) {
        this.idCriador = idCriador;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public PublicKey getChaveCriador() {
        return chaveCriador;
    }

    public void setChaveCriador(PublicKey chaveCriador) {
        this.chaveCriador = chaveCriador;
    }

    public Long getNumeroMagico() {
        return numeroMagico;
    }

    public void setNumeroMagico(Long numeroMagico) {
        this.numeroMagico = numeroMagico;
    }

    public byte[] getAssinaturaMaster() {
        return assinaturaMaster;
    }

    public void setAssinaturaMaster(byte[] assinaturaMaster) {
        this.assinaturaMaster = assinaturaMaster;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

package br.ufsm.csi.seguranca.pila.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by cpol on 17/04/2018.
 */
public class Transacao implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idNovoDono;
    private Date dataTransacao;
    private byte[] assinaturaDono;

    public String getIdNovoDono() {
        return idNovoDono;
    }

    public void setIdNovoDono(String idNovoDono) {
        this.idNovoDono = idNovoDono;
    }

    public Date getDataTransacao() {
        return dataTransacao;
    }

    public void setDataTransacao(Date dataTransacao) {
        this.dataTransacao = dataTransacao;
    }

    public byte[] getAssinaturaDono() {
        return assinaturaDono;
    }

    public void setAssinaturaDono(byte[] assinaturaDono) {
        this.assinaturaDono = assinaturaDono;
    }
}

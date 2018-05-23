package br.ufsm.csi.seguranca.pila.model;

import java.io.Serializable;

/**
 * Created by cpol on 22/05/2018.
 */
public class MensagemFragmentada implements Serializable {

    private static final long serialVersionUID = 1L;
    private int sequencia;
    private boolean ultimo;

    public boolean isUltimo() {
        return ultimo;
    }

    public void setUltimo(boolean ultimo) {
        this.ultimo = ultimo;
    }

    public int getSequencia() {
        return sequencia;
    }

    public void setSequencia(int sequencia) {
        this.sequencia = sequencia;
    }

    private byte[] fragmento;

    public byte[] getFragmento() {
        return fragmento;
    }

    public void setFragmento(byte[] fragmento) {
        if (fragmento == null || fragmento.length > 1000) {
            throw new IllegalArgumentException("fragmento não pode ter mais de 1000 bytes.");
        }
        this.fragmento = fragmento;
    }
}

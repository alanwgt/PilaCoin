//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package br.ufsm.csi.seguranca.server;

import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.model.ObjetoTroca;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.model.Mensagem.TipoMensagem;
import br.ufsm.csi.seguranca.server.model.Usuario;
import br.ufsm.csi.seguranca.util.RSAUtil;
import com.alanwgt.console.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MasterServer {
    private long ID_PILA = 0L;
    private Map<InetAddress, Usuario> usuarios = new HashMap();
    private Map<Long, PilaCoin> pilas = new HashMap();

    public MasterServer() {
    }

    private synchronized Long getID() {
        return ++this.ID_PILA;
    }

    public static void main(String[] args) {
        (new MasterServer()).iniciaThreads();
    }

    private void iniciaThreads() {
        (new Thread(new MasterServer.TCPServer())).start();
        (new Thread(new MasterServer.UDPServer())).start();
    }

    private ObjetoTroca criaObjetoTroca(SecretKey chaveSessao, PilaCoin pilaCoin) throws Exception {
        ObjetoTroca objetoTroca = new ObjetoTroca();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(1, chaveSessao);
        byte[] objeto = serializaObjeto(pilaCoin);
        byte[] objetoCripto = cipher.doFinal(objeto);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(objeto);
        Cipher cipherRSA = Cipher.getInstance("RSA");
        cipherRSA.init(1, RSAUtil.getPrivateKey("Master_private_key.der"));
        objetoTroca.setAssinatura(cipherRSA.doFinal(hash));
        objetoTroca.setObjetoSerializadoCriptografado(objetoCripto);
        return objetoTroca;
    }

    private String validaPilaCriado(byte[] hashPila, PilaCoin pilaCoin, InetAddress endereco) {
        Usuario usuario = (Usuario)this.usuarios.get(endereco);
        if (this.pilas.get(pilaCoin.getNumeroMagico()) != null) {
            return "Número mágico já utilizado.";
        } else {
            if (usuario != null && usuario.getChavePublica().equals(pilaCoin.getChaveCriador())) {
                BigInteger bigInteger = new BigInteger(1, hashPila);
                Logger.error(bigInteger.toString());
                Logger.error(Integer.toString(bigInteger.intValue()));
                if (bigInteger.compareTo(new BigInteger("99999998000000000000000000000000000000000000000000000000000000000000000")) < 0) {
                    return null;
                }
            }

            return "Assinatura inválida.";
        }
    }

    private SecretKey decifraChaveSessao(byte[] chaveSessao) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, RSAUtil.getPrivateKey("Master_private_key.der"));
        byte[] chave = cipher.doFinal(chaveSessao);
        return new SecretKeySpec(chave, "AES");
    }

    private void assinaMensagem(Mensagem mensagem) throws Exception {
        byte[] mSer = serializaObjeto(mensagem);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(mSer);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, RSAUtil.getPrivateKey("./Master_private_key.der"));
        mensagem.setAssinatura(cipher.doFinal(hash));
    }

    private void assinaPila(PilaCoin pila) throws Exception {
        byte[] mSer = serializaObjeto(pila);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(mSer);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, RSAUtil.getPrivateKey("./Master_private_key.der"));
        pila.setAssinaturaMaster(cipher.doFinal(hash));
    }

    public static byte[] serializaObjeto(Serializable obj) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(obj);
        return bout.toByteArray();
    }

    public static Serializable deserializaObjeto(byte[] obj) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bin = new ByteArrayInputStream(obj);
        ObjectInputStream in = new ObjectInputStream(bin);
        return (Serializable)in.readObject();
    }

    private class UDPServer implements Runnable {
        private UDPServer() {
        }

        public void run() {
            byte[] receiveData = new byte[1500];

            try {
                System.out.println("[UDP Server] Escutando porta 3333.");
                DatagramSocket serverSocket = new DatagramSocket(3333);

                while(true) {
                    DatagramPacket receivePacket;
                    Mensagem mensagem;
                    while(true) {
                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        ByteArrayInputStream in = new ByteArrayInputStream(receivePacket.getData());
                        ObjectInputStream objectInputStream = new ObjectInputStream(in);
                        mensagem = null;

                        try {
                            mensagem = (Mensagem)objectInputStream.readObject();
                            break;
                        } catch (InvalidClassException | ClassNotFoundException var11) {
                            System.out.println("[UDP Server] Pacote inválido recebido de " + receivePacket.getAddress() + " (" + var11.getMessage() + ").");
                        }
                    }

                    System.out.println("[UDP Server] Recebido pacote tipo " + mensagem.getTipo() + " de " + receivePacket.getAddress() + " (id = " + mensagem.getIdOrigem() + ") .");
                    if (mensagem.getTipo() == TipoMensagem.DISCOVER) {
                        Usuario usuario = new Usuario();
                        usuario.setChavePublica(mensagem.getChavePublica());
                        usuario.setEndereco(receivePacket.getAddress());
                        usuario.setId(mensagem.getIdOrigem());
                        MasterServer.this.usuarios.put(usuario.getEndereco(), usuario);
                        Mensagem resposta = new Mensagem();
                        resposta.setTipo(TipoMensagem.DISCOVER_RESP);
                        resposta.setChavePublica(RSAUtil.getMasterPublicKey());
                        resposta.setEndereco(InetAddress.getLocalHost());
                        resposta.setPorta(4444);
                        resposta.setMaster(true);
                        MasterServer.this.assinaMensagem(resposta);
                        byte[] respSerial = MasterServer.serializaObjeto(resposta);
                        DatagramPacket sendPacket = new DatagramPacket(respSerial, respSerial.length, receivePacket.getAddress(), mensagem.getPorta());
                        serverSocket.send(sendPacket);
                        System.out.println("[UDP Server] Enviado " + resposta.getTipo() + " para " + receivePacket.getAddress() + ".");
                    }
                }
            } catch (SocketException var12) {
                var12.printStackTrace();
            } catch (IOException var13) {
                var13.printStackTrace();
            } catch (Exception var14) {
                var14.printStackTrace();
            }

        }
    }

    private class TCPServer implements Runnable {
        private TCPServer() {
        }

        public void run() {
            try {
                ServerSocket ss = new ServerSocket(4444);
                System.out.println("[TCP Server] Ouvindo porta 4444.");

                while(true) {
                    while(true) {
                        try {
                            Socket s = ss.accept();
                            System.out.println("[TCP Server] Recebida conexão de " + s.getInetAddress());
                            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                            ObjetoTroca objetoTroca = (ObjetoTroca)in.readObject();
                            SecretKey chaveSessao = MasterServer.this.decifraChaveSessao(objetoTroca.getChaveSessao());
                            Cipher cipherAES = Cipher.getInstance("AES");
                            cipherAES.init(2, chaveSessao);
                            if (MasterServer.this.usuarios.get(s.getInetAddress()) == null) {
                                System.out.println("[TCP Server] Usuário desconhecido " + s.getInetAddress());
                                Mensagem respostax = new Mensagem();
                                respostax.setTipo(TipoMensagem.ERRO);
                                respostax.setChavePublica(RSAUtil.getMasterPublicKey());
                                respostax.setEndereco(InetAddress.getLocalHost());
                                respostax.setPorta(4444);
                                respostax.setMaster(true);
                                respostax.setErro("Usuário não reconhecido. Não houve mensagem DISCOVER anterior.");
                                MasterServer.this.assinaMensagem(respostax);
                                out.writeObject(respostax);
                                s.close();
                            } else {
                                byte[] bPila = cipherAES.doFinal(objetoTroca.getObjetoSerializadoCriptografado());
                                Logger.info("encryptedSerialized " + Base64.getEncoder().encodeToString(objetoTroca.getObjetoSerializadoCriptografado()));
                                Logger.info("serialized bpila: " + Base64.getEncoder().encodeToString(bPila));
                                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                                byte[] hashPila = digest.digest(bPila);
                                Logger.info("hash pila: " + Base64.getEncoder().encodeToString(hashPila));
                                Cipher cipherRSA = Cipher.getInstance("RSA");
                                cipherRSA.init(2, ((Usuario)MasterServer.this.usuarios.get(s.getInetAddress())).getChavePublica());
                                byte[] hashAssinatura = cipherRSA.doFinal(objetoTroca.getAssinatura());
                                Logger.info("sent signature: " + Base64.getEncoder().encodeToString(objetoTroca.getAssinatura()));
                                Logger.info("decrypted signature: " + Base64.getEncoder().encodeToString(hashAssinatura));
                                if (Arrays.equals(hashPila, hashAssinatura)) {
                                    PilaCoin pilaCoin = (PilaCoin)MasterServer.deserializaObjeto(bPila);
                                    String msgErro;
                                    if ((msgErro = MasterServer.this.validaPilaCriado(hashPila, pilaCoin, s.getInetAddress())) == null) {
                                        pilaCoin.setId(MasterServer.this.getID());
                                        MasterServer.this.assinaPila(pilaCoin);
                                        MasterServer.this.pilas.put(pilaCoin.getNumeroMagico(), pilaCoin);
                                        objetoTroca = MasterServer.this.criaObjetoTroca(chaveSessao, pilaCoin);
                                        out.writeObject(objetoTroca);
                                        s.close();
                                        System.out.println("[TCP Server] Pila coin " + pilaCoin.getId() + " válido recebido do usuário " + pilaCoin.getIdCriador() + ".");
                                    } else {
                                        Mensagem resposta = new Mensagem();
                                        resposta.setTipo(TipoMensagem.ERRO);
                                        resposta.setChavePublica(RSAUtil.getMasterPublicKey());
                                        resposta.setEndereco(InetAddress.getLocalHost());
                                        resposta.setPorta(4444);
                                        resposta.setMaster(true);
                                        resposta.setErro("[TCP Server] Pila inválido: " + msgErro);
                                        MasterServer.this.assinaMensagem(resposta);
                                        out.writeObject(resposta);
                                        s.close();
                                    }
                                } else {
                                    System.out.println("[TCP Server] Assinatura inválida " + s.getInetAddress());
                                    Mensagem respostaxx = new Mensagem();
                                    respostaxx.setTipo(TipoMensagem.ERRO);
                                    respostaxx.setChavePublica(RSAUtil.getMasterPublicKey());
                                    respostaxx.setEndereco(InetAddress.getLocalHost());
                                    respostaxx.setPorta(4444);
                                    respostaxx.setMaster(true);
                                    respostaxx.setErro("Assinatura inválida.");
                                    MasterServer.this.assinaMensagem(respostaxx);
                                    out.writeObject(respostaxx);
                                    s.close();
                                }
                            }
                        } catch (Exception var16) {
                            var16.printStackTrace();
                        }
                    }
                }
            } catch (IOException var17) {
                var17.printStackTrace();
            }
        }
    }
}

package br.ufsm.csi.seguranca.client;


import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.model.Mensagem.TipoMensagem;
import br.ufsm.csi.seguranca.server.MasterServer;
import br.ufsm.csi.seguranca.util.RSAUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;

public class Client {
    public Client() {
    }

    public static void main(String[] args) {
        (new Client()).iniciaThreads();
    }

    private void iniciaThreads() {
        (new Thread(new Client.UDPClient())).start();
        (new Thread(new Client.UDPServer())).start();
    }

    private boolean validaAssinatura(byte[] assinatura, Mensagem mensagem) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, RSAUtil.getMasterPublicKey());
        byte[] hashAssinatura = cipher.doFinal(assinatura);
        byte[] mSer = MasterServer.serializaObjeto(mensagem);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(mSer);
        return Arrays.equals(hashAssinatura, hash);
    }

    private class UDPServer implements Runnable {
        private UDPServer() {
        }

        public void run() {
            byte[] receiveData = new byte[1500];

            try {
                System.out.println("[UDP Client] Escutando porta 4444.");
                DatagramSocket serverSocket = new DatagramSocket(4444);

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
                        } catch (ClassNotFoundException var8) {
                            System.out.println("[UDP Client] Pacote inválido recebido de " + receivePacket.getAddress() + ".");
                        }
                    }

                    System.out.println("[UDP Client] Recebido pacote tipo " + mensagem.getTipo() + " de " + receivePacket.getAddress() + ".");
                    if (mensagem.getTipo() == TipoMensagem.DISCOVER_RESP) {
                        byte[] assinatura = mensagem.getAssinatura();
                        mensagem.setAssinatura((byte[])null);
                        if (Client.this.validaAssinatura(assinatura, mensagem)) {
                            System.out.println("[UDP Client] Pacote DISCOVER_REST válido recebido de " + receivePacket.getAddress() + ".");
                        } else {
                            System.out.println("[UDP Client] Pacote DISCOVER_REST inválido recebido de " + receivePacket.getAddress() + ".");
                        }
                    }
                }
            } catch (SocketException var9) {
                var9.printStackTrace();
            } catch (IOException var10) {
                var10.printStackTrace();
            } catch (Exception var11) {
                var11.printStackTrace();
            }

        }
    }

    private class UDPClient implements Runnable {
        private UDPClient() {
        }

        public void run() {
            try {
                DatagramSocket serverSocket = new DatagramSocket();

                while(true) {
                    Mensagem mensagem = new Mensagem();
                    mensagem.setTipo(TipoMensagem.DISCOVER);
                    mensagem.setIdOrigem("rafael");
                    mensagem.setPorta(4444);
                    byte[] sendData = MasterServer.serializaObjeto(mensagem);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 3333);
                    serverSocket.send(sendPacket);
                    System.out.println("[UDP Client] Enviou Mensagem DISCOVER.");
                    Thread.sleep(15000L);
                }
            } catch (SocketException var5) {
                var5.printStackTrace();
            } catch (IOException var6) {
                var6.printStackTrace();
            } catch (Exception var7) {
                var7.printStackTrace();
            }

        }
    }
}

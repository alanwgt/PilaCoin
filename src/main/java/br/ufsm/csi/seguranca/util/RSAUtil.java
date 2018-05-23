package br.ufsm.csi.seguranca.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtil {

    /*
        Atenção: para gerar o seu par de chaves instale o programa OPENSSL executando os seguintes comandos:

            $ openssl genrsa -out private_key.pem 2048
            $ openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out Master_private_key.der -nocrypt
            $ openssl rsa -in private_key.pem -pubout -outform DER -out Master_public_key.der

        A sua chave privada estará no arquivo Master_private_key.der e a pública no arquivo Master_public_key.der. Utilize esta classe
        para carregar estes arquivos em objetos PublicKey e PrivateKey do Java.
    */

    public static PrivateKey getPrivateKey(String filename) throws Exception {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }


    public static PublicKey getPublicKey(String filename) throws Exception
    {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private static PublicKey MASTER_PUB_KEY;

    public static PublicKey getMasterPublicKey() throws Exception {
        synchronized (RSAUtil.class) {
            if (MASTER_PUB_KEY == null) {
                byte[] keyBytes = Files.readAllBytes(Paths.get("Master_public_key.der"));
                X509EncodedKeySpec spec =
                        new X509EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                MASTER_PUB_KEY = kf.generatePublic(spec);
            }
        }
        return MASTER_PUB_KEY;
    }

}

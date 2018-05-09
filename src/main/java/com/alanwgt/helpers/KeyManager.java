package com.alanwgt.helpers;

import java.nio.file.Paths;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import com.alanwgt.console.Logger;
import com.alanwgt.security.AESCipher;
import com.alanwgt.security.KeyMaster;
import com.alanwgt.security.RSACipher;


public final class KeyManager {

    private static final String DEFAULT_PATH = Paths.get(System.getProperty("user.dir"), "keys").toString();
    public static final String PUBLIC_KEY_PATH = Paths.get(DEFAULT_PATH, "id_rsa.pub").toString();
    public static final String SERVER_PUBLIC_KEY_PATH = Paths.get(DEFAULT_PATH, "Master_public_key.der").toString();
    public static final String PRIVATE_KEY_PATH = Paths.get(DEFAULT_PATH, "id_rsa").toString();

    private static Key secretKey, publicKey, privateKey, masterPublicKey;
    private static RSACipher publicCipher, privateCipher, masterCipher;
    private static AESCipher secretCipher;

    private KeyManager() {

    }

    public static void init() {
        try {
            privateKey = KeyMaster.loadFromFile(PrivateKey.class, PRIVATE_KEY_PATH);
            publicKey = KeyMaster.loadFromFile(PublicKey.class, PUBLIC_KEY_PATH);
            masterPublicKey = KeyMaster.loadFromByteFile(PublicKey.class, SERVER_PUBLIC_KEY_PATH);

            publicCipher = new RSACipher(publicKey);
            privateCipher = new RSACipher(privateKey);
            masterCipher = new RSACipher(masterPublicKey);

            masterCipher.init();
            privateCipher.init();
            publicCipher.init();

            toB64String();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Key getSecretKey() {
        return secretKey;
    }

    public static Key getPublicKey() {
        return publicKey;
    }

    public static Key getPrivateKey() {
        return privateKey;
    }

    public static Key getMasterPublicKey() {
        return masterPublicKey;
    }

    public static RSACipher getPublicCipher() {
        return publicCipher;
    }

    public static RSACipher getPrivateCipher() {
        return privateCipher;
    }

    public static RSACipher getMasterCipher() {
        return masterCipher;
    }

    public static AESCipher getSecretCipher() {
        return secretCipher;
    }

    public static void toB64String() {
        Logger.debug(
                "my public key:     " + Base64.getEncoder().encodeToString(publicKey.getEncoded()),
                "master public key: " + Base64.getEncoder().encodeToString(masterPublicKey.getEncoded())
        );
    }
}
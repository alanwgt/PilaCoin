package com.alanwgt.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public abstract class KeyMaster {

    public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize);

        return generator.generateKeyPair();
    }

    public static SecretKey generateSecretKey(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(keySize);

        return generator.generateKey();
    }

    public static Key load(Class<? extends Key> keyClass, String b64Encoded) throws InvalidClassException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] key = Base64.getDecoder().decode(b64Encoded);
        return load(keyClass, key);
    }

    public static Key load(Class keyClass, byte[] key) throws InvalidClassException, InvalidKeySpecException, NoSuchAlgorithmException {
        if (keyClass.equals(SecretKey.class)) {
            return loadSecretKey(key);
        }

        if (keyClass.equals(PublicKey.class)) {
            return loadPublicKey(key);
        }

        if (keyClass.equals(PrivateKey.class)) {
            return loadPrivateKey(key);
        }

        throw new InvalidClassException("The provided class must be subtype of Key");
    }

    public static Key loadSecretKey(byte[] key) {
        return new SecretKeySpec(key, 0, key.length, "AES");
    }

    public static Key loadPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static Key loadPrivateKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(encodedKeySpec);
    }

//    public static byte[] sign(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
//        Signature signature = Signature.getInstance("SHA256withRSA");
//        signature.initSign(privateKey);
//        signature.update(data);
//
//        return signature.sign();
//    }

    public static boolean verify(String plainText, byte[] bSignature, PublicKey publicKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(bSignature);

        return signature.verify(bSignature);
    }

    public static void store(String path, Key key) throws IOException {
        FileOutputStream fos = new FileOutputStream(path, false);
        fos.write(Base64.getEncoder().encodeToString(key.getEncoded()).getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    public static Key loadFromFile(Class<? extends Key> keyClass, String path) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }

        return load(keyClass, sb.toString().trim());
    }

    public static Key loadFromByteFile(Class<? extends Key> keyClass, String path) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        Path p = Paths.get(path);
        return load(keyClass, Files.readAllBytes(p));
    }

    public static String toString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}

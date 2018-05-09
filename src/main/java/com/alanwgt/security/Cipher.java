package com.alanwgt.security;

import com.alanwgt.console.Logger;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.util.Base64;

public abstract class Cipher {

    private final Key key;
    private final String cipherInstance;
    private javax.crypto.Cipher cipher;

    public Cipher(@Nonnull Key key, String cipherInstance) {
        this.key = key;
        this.cipherInstance = cipherInstance;
    }

    public Key getKey() {
        return key;
    }

    protected javax.crypto.Cipher getCipher() {
        return cipher;
    }

    public void init() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = javax.crypto.Cipher.getInstance(cipherInstance);
    }

    protected abstract Encrypted encryptData(byte[] data) throws BadPaddingException, IllegalBlockSizeException;
    protected abstract byte[] decryptData(Encrypted encrypted) throws BadPaddingException, IllegalBlockSizeException;

    public Encrypted encrypt(byte[] data) throws KeyNotDefinedException, CipherNotInitializedException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (key == null) {
            throw new KeyNotDefinedException();
        }
        if (cipher == null) {
            throw new CipherNotInitializedException();
        }

        Logger.debug("encrypting: " + Base64.getEncoder().encodeToString(data));
//        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, new SecureRandom());
//        byte[] iv = new byte[16];
//        Arrays.fill(iv, (byte) 0)
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        return encryptData(data);
    }

    public byte[] decrypt(Encrypted encrypted) throws KeyNotDefinedException, CipherNotInitializedException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        if (key == null) {
            throw new KeyNotDefinedException();
        }
        if (cipher == null) {
            throw new CipherNotInitializedException();
        }

        Logger.debug("decrypting: " + Base64.getEncoder().encodeToString(encrypted.getData()));
        if (encrypted.iv == null) {
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
        } else {
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, new IvParameterSpec(encrypted.getIv()));
        }
        return decryptData(encrypted);
    }

    public static class Encrypted {
        private final byte[] data, iv;

        public Encrypted(byte[] data, @Nullable byte[] iv) {
            this.data = data;
            this.iv = iv;
        }

        public byte[] getData() {
            return data;
        }

        public byte[] getIv() {
            return iv;
        }

        public String getB64Data() {
            return Base64.getEncoder().encodeToString(data);
        }

        @Nullable
        public String getB64Iv() {
            if (iv == null) {
                return null;
            }
            return Base64.getEncoder().encodeToString(iv);
        }
    }

}

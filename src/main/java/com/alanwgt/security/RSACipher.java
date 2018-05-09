package com.alanwgt.security;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;

public class RSACipher extends Cipher {

    public static final String DEFAULT_RSA_CIPHER_INSTANCE = "RSA";

    public RSACipher(Key key) {
        super(key, DEFAULT_RSA_CIPHER_INSTANCE);
    }

    @Override
    protected Encrypted encryptData(byte[] data) throws BadPaddingException, IllegalBlockSizeException {
        byte[] cipherData = getCipher().doFinal(data);
        return new Encrypted(cipherData, null);
    }

    @Override
    protected byte[] decryptData(Encrypted encrypted) throws BadPaddingException, IllegalBlockSizeException {
        return getCipher().doFinal(encrypted.getData());
    }

    public Encrypted wrapKey(Key toWrap) throws InvalidKeyException, IllegalBlockSizeException {
        getCipher().init(javax.crypto.Cipher.WRAP_MODE, getKey(), new SecureRandom());
        return new Encrypted(
                getCipher().wrap(toWrap),
                null
        );
    }
}

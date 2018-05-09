package com.alanwgt.security;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.Key;

public class AESCipher extends Cipher {

    public static final String DEFAULT_AES_CIPHER_INSTANCE = "AES";

    public AESCipher(Key key) {
        super(key, DEFAULT_AES_CIPHER_INSTANCE);
    }

    @Override
    protected Encrypted encryptData(byte[] data) throws BadPaddingException, IllegalBlockSizeException {
        return new Encrypted(
                getCipher().doFinal(data),
                getCipher().getIV()
        );
    }

    @Override
    protected byte[] decryptData(Encrypted encrypted) throws BadPaddingException, IllegalBlockSizeException {
        return getCipher().doFinal(encrypted.getData());
    }
}

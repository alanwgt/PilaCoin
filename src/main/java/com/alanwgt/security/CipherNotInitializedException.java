package com.alanwgt.security;

public class CipherNotInitializedException extends Exception {
    public CipherNotInitializedException() {
        super("The Cipher was not properly initialized!");
    }
}

package com.alanwgt.security;

public class KeyNotDefinedException extends Exception {
    public KeyNotDefinedException() {
        super("A Key must be provided!");
    }
}

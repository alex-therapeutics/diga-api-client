package com.alextherapeutics.diga;

/**
 * Something went wrong during an encryption attempt
 */
public class DigaEncryptionException extends Exception {
    public DigaEncryptionException(String message) {
        super(message);
    }
}

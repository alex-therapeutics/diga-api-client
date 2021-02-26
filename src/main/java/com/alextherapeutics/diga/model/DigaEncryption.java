package com.alextherapeutics.diga.model;

import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconException;
import de.tk.opensource.secon.Subscriber;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Attempt to encrypt an inputstream using SECON
 */
@Builder
@Slf4j
public class DigaEncryption {
    /**
     * The encrypting subscriber
     */
    @NonNull
    private Subscriber subscriber;
    /**
     * The input to encrypt
     */
    @NonNull
    private byte[] encryptionTarget;
    /**
     * The alias of the key in the public key directory
     */
    @NonNull
    private String recipientAlias;

    /**
     * Encrypt the contents as a byte array output stream
     * @return
     */
    public ByteArrayOutputStream encrypt() throws IOException, SeconException {
        try (var input = new ByteArrayInputStream(encryptionTarget)) {
            try (var output = new ByteArrayOutputStream()) {
                SECON.copy(
                        () -> new ByteArrayInputStream(input.readAllBytes()),
                        subscriber.signAndEncryptTo(
                                () -> output, recipientAlias
                        )
                );
                return output;
            }
        }
    }
}

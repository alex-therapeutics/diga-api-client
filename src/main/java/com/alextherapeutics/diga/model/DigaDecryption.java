package com.alextherapeutics.diga.model;

import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconException;
import de.tk.opensource.secon.Subscriber;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Attempt to decrypt a SECON encrypted inputstream
 */
@Builder
@Slf4j
public class DigaDecryption {
    /**
     * The decrypting subscriber
     */
    @NonNull
    private Subscriber subscriber;
    /**
     * The body to decrypt
     */
    @NonNull
    private InputStream decryptionTarget;

    public ByteArrayOutputStream decrypt() throws IOException, SeconException {
        try (var outputStream = new ByteArrayOutputStream()) {
            SECON.copy(
                    subscriber.decryptAndVerifyFrom(
                            () -> decryptionTarget
                    ),
                    () -> outputStream
            );
            return outputStream;
        }
    }
}

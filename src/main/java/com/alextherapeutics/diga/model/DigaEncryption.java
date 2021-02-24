package com.alextherapeutics.diga.model;

import com.alextherapeutics.diga.DigaEncryptionException;
import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconException;
import de.tk.opensource.secon.Subscriber;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An encryption attempt
 */
@Builder
@Slf4j
public class DigaEncryption {
    /**
     * The
     */
    @NonNull
    private Subscriber subscriber;
    /**
     * The input to encrypt
     */
    @NonNull
    private InputStream encryptionTarget;
    /**
     * The alias of the key in the public key directory
     */
    @NonNull
    private String recipientAlias;

    /**
     * Encrypt the contents as a byte array output stream
     * @return
     * @throws DigaEncryptionException
     */
    public ByteArrayOutputStream encrypt() throws DigaEncryptionException {
        try (var output = new ByteArrayOutputStream()) {
            SECON.copy(
                    () -> new ByteArrayInputStream(encryptionTarget.readAllBytes()),
                    subscriber.signAndEncryptTo(
                            () -> output, recipientAlias
                    )
            );
            return output;
        } catch (SeconException | IOException e) {
            log.error("Error during encryption:", e);
            throw new DigaEncryptionException(e.getMessage());
        }
    }
}

package com.alextherapeutics.diga.model;

import de.tk.opensource.secon.*;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    private byte[] decryptionTarget;

    public ByteArrayOutputStream decrypt() throws IOException, SeconException {
        try (var outputStream = new ByteArrayOutputStream()) {
            try (var inputStream = new ByteArrayInputStream(decryptionTarget)) {
                SECON.copy(
                        subscriber.decryptAndVerifyFrom(
                                () -> inputStream,
                                Verifier.NULL
                        ),
                        () -> outputStream
                );
                return outputStream;
            } catch (CertificateNotFoundException e) {
                // there seems to be something wrong in the key list from itsg which means some certificates have the wrong serial number on returning a response. in the future hopefully this is not a necessary catch
                log.debug("Failed to validate sender certificate after (successfully) finishing decrypting the content.\n Since we wouldnt get this far without the server and us having the correct private and public keys for eachother, we pass the decrypted content as a success.");
                return outputStream;
            }
        }
    }
}

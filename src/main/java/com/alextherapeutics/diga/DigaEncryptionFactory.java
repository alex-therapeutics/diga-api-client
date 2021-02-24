package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaEncryption;
import de.tk.opensource.secon.*;
import lombok.Builder;
import lombok.NonNull;

import java.io.ByteArrayInputStream;

/**
 * Set up a factory for creating new encrypted items using pre-configured keystores.
 */
public class DigaEncryptionFactory {
    // input fields
    private byte[] privateKeyBytes;
    private String privateKeyAlias;
    private String privateKeyPassword;
    private byte[] publicKeysBytes;
    private String publicKeyDirectoryPassword;

    // initialized fields
    private Identity identity;
    private Directory publicKeyDirectory;
    private Subscriber subscriber;

    @Builder
    public DigaEncryptionFactory(
            @NonNull byte[] privateKeyBytes,
            @NonNull String privateKeyAlias,
            @NonNull String privateKeyPassword,
            @NonNull byte[] publicKeysBytes,
            @NonNull String publicKeyDirectoryPassword
    ) throws SeconException {
        this.privateKeyBytes = privateKeyBytes;
        this.privateKeyAlias = privateKeyAlias;
        this.privateKeyPassword = privateKeyPassword;
        this.publicKeysBytes = publicKeysBytes;
        this.publicKeyDirectoryPassword = publicKeyDirectoryPassword;
        init();
    }

    /**
     * Start a new encryption attempt with the factory defaults.
     * @return
     */
    public DigaEncryption.DigaEncryptionBuilder newEncryption() {
        return DigaEncryption.builder()
                .subscriber(subscriber);
    }

    private void init() throws SeconException {
        this.identity = SECON.identity(
                SECON.keyStore(
                        () -> new ByteArrayInputStream(privateKeyBytes),
                        privateKeyPassword::toCharArray
                ),
                privateKeyAlias,
                privateKeyPassword::toCharArray
        );
        this.publicKeyDirectory = SECON.directory(
                SECON.keyStore(
                        () -> new ByteArrayInputStream(publicKeysBytes),
                        publicKeyDirectoryPassword::toCharArray
                )
        );
        this.subscriber = SECON.subscriber(
                this.identity,
                this.publicKeyDirectory
        );
    }
}

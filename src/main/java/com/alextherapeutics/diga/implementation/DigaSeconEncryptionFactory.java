package com.alextherapeutics.diga.implementation;

import com.alextherapeutics.diga.DigaEncryptionFactory;
import com.alextherapeutics.diga.model.DigaDecryption;
import com.alextherapeutics.diga.model.DigaEncryption;
import de.tk.opensource.secon.*;
import lombok.Builder;
import lombok.NonNull;

import java.io.ByteArrayInputStream;

/**
 * A {@link DigaEncryptionFactory} implemented using the {@link SECON} library.
 */
public class DigaSeconEncryptionFactory implements DigaEncryptionFactory {
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
    public DigaSeconEncryptionFactory(
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
    @Override
    public DigaEncryption.DigaEncryptionBuilder newEncryption() {
        return DigaEncryption.builder()
                .subscriber(subscriber);
    }
    @Override
    public DigaDecryption.DigaDecryptionBuilder newDecryption() {
        return DigaDecryption.builder()
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

/*
 * Copyright 2021-2021 Alex Therapeutics AB and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

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

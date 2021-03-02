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

package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.InputStream;

/**
 * Required settings for initializing a DiGA API Client
 */
@Builder
@Getter
public class DigaApiClientSettings {
    /**
     * The contents of the mapping file for health insurance companies
     * located at https://kkv.gkv-diga.de/
     */
    @NonNull
    private InputStream healthInsuranceMappingFile;
    /**
     * The contents of the keystore file (PKCS12, .p12 extension) containing the sender company's private key
     * for signing requests and for decrypting responses
     */
    @NonNull
    private InputStream privateKeyStoreFile;
    /**
     * The contents of a keystore file (PKCS12, .p12 extension) containing all health insurance companies certificates
     * for encrypting requests
     */
    @NonNull
    private InputStream healthInsurancePublicKeyStoreFile;
    /**
     * Password to the private key store file
     */
    @NonNull
    private String privateKeyStorePassword;
    /**
     * Alias of the private key in the private key store file
     */
    @NonNull
    private String privateKeyAlias;
    /**
     * Password to the health insurance companies key store file
     */
    @NonNull
    private String healthInsurancePublicKeyStorePassword;
    /**
     * The IK number of your company sending requests from this API client.
     */
    @NonNull
    private String senderIkNUmber;
    /**
     * The DiGA ID of the DiGA which is using this client. If you are serving more than one DiGA from
     * this backend, you will need two instances of the client.
     */
    @NonNull
    private String senderDigaId;
}

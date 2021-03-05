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

import com.alextherapeutics.diga.DigaHttpClientException;
import nl.altindag.ssl.SSLFactory;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.net.ssl.X509ExtendedTrustManager;
import java.security.KeyStore;
import java.util.Optional;

class DigaOkHttpClientTest {
    private OkHttpClient okHttpClient;
    private DigaOkHttpClient client;

    @BeforeEach
    void init() throws DigaHttpClientException {
        // setup so you can check the okhttpclient mock for interactions
        try (var keystore = Mockito.mockStatic(KeyStore.class)) {
            var mockStore = Mockito.mock(KeyStore.class);
            keystore.when(() -> { KeyStore.getInstance(Mockito.anyString()); })
                    .thenReturn(mockStore);
            try (var sslFactory = Mockito.mockStatic(SSLFactory.class)) {
                var mockSslBuilder = Mockito.mock(SSLFactory.Builder.class);
                sslFactory.when(SSLFactory::builder).thenReturn(mockSslBuilder);
                Mockito.when(mockSslBuilder.withDefaultTrustMaterial()).thenReturn(mockSslBuilder);
                Mockito.when(mockSslBuilder.withSystemTrustMaterial()).thenReturn(mockSslBuilder);
                Mockito.when(mockSslBuilder.withTrustMaterial(Mockito.any(KeyStore.class))).thenReturn(mockSslBuilder);
                Mockito.when(mockSslBuilder.withIdentityMaterial(Mockito.any(KeyStore.class), Mockito.any(char[].class))).thenReturn(mockSslBuilder);
                var mockSslFactory = Mockito.mock(SSLFactory.class);
                Mockito.when(mockSslBuilder.build()).thenReturn(mockSslFactory);
                Mockito.when(mockSslFactory.getTrustManager()).thenReturn(Optional.of(Mockito.mock(X509ExtendedTrustManager.class)));
                okHttpClient = Mockito.mock(OkHttpClient.class);
                try (var mockedOkHttpBuilder = Mockito.mockConstruction(
                        OkHttpClient.Builder.class,
                        (mock, context) -> {
                            Mockito.when(mock.sslSocketFactory(Mockito.any(), Mockito.any())).thenReturn(mock);
                            Mockito.when(mock.hostnameVerifier(Mockito.any())).thenReturn(mock);
                            Mockito.when(mock.build()).thenReturn(okHttpClient);
                        }
                )) {
                    client = DigaOkHttpClient.builder()
                            .certificatesFileContent(new byte[]{})
                            .certificatesPassword("dummy")
                            .keyStoreFileContent(new byte[]{})
                            .keyStorePassword("dummy")
                            .build();
                }
            }
        }
    }
    // not sure what to test here.. but the class is set up for later
    @Test
    void test() {
    }

}
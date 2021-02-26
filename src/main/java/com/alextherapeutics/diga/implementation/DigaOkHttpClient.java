package com.alextherapeutics.diga.implementation;

import com.alextherapeutics.diga.DigaHttpClient;
import com.alextherapeutics.diga.DigaHttpClientException;
import com.alextherapeutics.diga.DigaUtils;
import com.alextherapeutics.diga.model.DigaApiHttpRequest;
import com.alextherapeutics.diga.model.DigaApiHttpResponse;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import okhttp3.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Default HTTP client using OkHttp configured to trust the insurance company certificates
 * and provide yuor own certificate with each request.
 */
@Slf4j
public class DigaOkHttpClient implements DigaHttpClient {
    private byte[] keyStoreFileContent;
    private String keyStorePassword;
    private byte[] certificatesFileContent;
    private String certificatesPassword;

    private OkHttpClient client;

    @Builder
    public DigaOkHttpClient(
            @NonNull byte[] keyStoreFileContent,
            @NonNull String keyStorePassword,
            @NonNull byte[] certificatesFileContent,
            @NonNull String certificatesPassword
    ) throws DigaHttpClientException {
        this.keyStoreFileContent = keyStoreFileContent;
        this.keyStorePassword = keyStorePassword;
        this.certificatesFileContent = certificatesFileContent;
        this.certificatesPassword = certificatesPassword;
        init();
    }

    @Override
    public DigaApiHttpResponse post(DigaApiHttpRequest request) throws DigaHttpClientException {
        try {
            var httpResponse = client.newCall(
                    toOkHttpRequest(request)
            ).execute();
            return parseResponse(httpResponse);
        } catch (IOException e) {
            log.error("Http request failed", e);
            throw new DigaHttpClientException(e.getMessage());
        }
    }

    private DigaApiHttpResponse parseResponse(Response okHttpResponse) throws IOException {
        var responseBuilder = DigaApiHttpResponse.builder()
                .statusCode(okHttpResponse.code());
        var multiPartReader = new MultipartReader(okHttpResponse.body());
        MultipartReader.Part nextPart = null;
        do {
            nextPart = multiPartReader.nextPart();
            responseBuilder = parsePart(nextPart, responseBuilder);
        }
        while (nextPart != null);
        multiPartReader.close();
        return responseBuilder.build();
    }

    private Request toOkHttpRequest(DigaApiHttpRequest digaApiHttpRequest) {
        var body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("iksender", DigaUtils.ikNumberWithoutPrefix(digaApiHttpRequest.getSenderIK()))
                .addFormDataPart("ikempfaenger", DigaUtils.ikNumberWithoutPrefix(digaApiHttpRequest.getRecipientIK()))
                .addFormDataPart("verfahren", digaApiHttpRequest.getVerfahren())
                .addFormDataPart("nutzdaten", "anfrage.cms", RequestBody.create(digaApiHttpRequest.getEncryptedContent()))
                .build();
        return new Request.Builder()
                .url(digaApiHttpRequest.getUrl())
                .post(body)
                .build();
    }

    private void init() throws DigaHttpClientException {
        try {
            var krankenKasseTrustStore = KeyStore.getInstance("PKCS12");
            krankenKasseTrustStore.load(new ByteArrayInputStream(certificatesFileContent), certificatesPassword.toCharArray());
            var clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(new ByteArrayInputStream(keyStoreFileContent), keyStorePassword.toCharArray());

            var sslFactory = SSLFactory.builder()
                    .withDefaultTrustMaterial()
                    .withSystemTrustMaterial()
                    .withTrustMaterial(krankenKasseTrustStore)
                    .withIdentityMaterial(clientStore, keyStorePassword.toCharArray())
                    .build();

            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslFactory.getSslSocketFactory(), sslFactory.getTrustManager().orElseThrow())
                    .hostnameVerifier(sslFactory.getHostnameVerifier())
                    .build();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            log.error("Failed to instantiate OkHttpClient", e);
            throw new DigaHttpClientException(e.getMessage());
        }
    }
    private boolean headerContainsFormDataName(Headers headers, String name) {
        var it = headers.iterator();
        var result = false;
        while (it.hasNext()) {
            var next = it.next();
            if (next.getSecond().contains(name)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private DigaApiHttpResponse.DigaApiHttpResponseBuilder parsePart(
                    MultipartReader.Part part,
                    DigaApiHttpResponse.DigaApiHttpResponseBuilder builder) throws IOException {
        if (part == null) {
            return builder;
        }
        var headers = part.headers();
        if (headerContainsFormDataName(headers, "iksender")) { // TODO make enum
            return builder.senderIK(
                    part.body().readString(StandardCharsets.UTF_8)
            );
        } else if (headerContainsFormDataName(headers, "ikempfaenger")) {
            return builder.recipientIK(
                    part.body().readString(StandardCharsets.UTF_8)
            );
        } else if (headerContainsFormDataName(headers, "verfahren")) {
            return builder.verfahren(
                    part.body().readString(StandardCharsets.UTF_8)
            );
        } else if (headerContainsFormDataName(headers, "nutzdaten")) {
            return builder.encryptedBody(
                    part.body().readByteArray()
            );
        }
        return builder;
    }
}

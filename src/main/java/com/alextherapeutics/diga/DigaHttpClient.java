package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaApiRequest;
import com.alextherapeutics.diga.model.DigaApiResponse;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Default HTTP client configured to trust the insurance company certificates and provide yuor own certificate with each request.
 */

@Slf4j
public class DigaHttpClient {
    private byte[] keyStoreFileContent;
    private String keyStorePassword;
    private byte[] certificatesFileContent;
    private String certificatesPassword;

    private OkHttpClient client;

    @Builder
    public DigaHttpClient(
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

    /**
     * POST a DigaApiRequest
     * @param request
     * @return
     * @throws DigaHttpClientException
     */
    public DigaApiResponse post(DigaApiRequest request) throws DigaHttpClientException {
        try {
            var httpResponse = client.newCall(
                    toOkHttpRequest(request)
            ).execute();
            return DigaApiResponse.builder()
                    .statusCode(httpResponse.code())
                    .body(httpResponse.body().string())
                    .build();

        } catch (IOException e) {
            log.error("Http request failed", e);
            throw new DigaHttpClientException(e.getMessage());
        }
    }

    /**
     * Return the API request as an OkHttpRequest
     * @return
     */
    private Request toOkHttpRequest(DigaApiRequest digaApiRequest) {
        var body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("iksender", DigaUtils.ikNumberWithoutPrefix(digaApiRequest.getSenderIK()))
                .addFormDataPart("ikempfaenger", DigaUtils.ikNumberWithoutPrefix(digaApiRequest.getRecipientIK()))
                .addFormDataPart("verfahren", digaApiRequest.getVerfahren())
                .addFormDataPart("nutzdaten", "anfrage.cms", RequestBody.create(digaApiRequest.getEncryptedContent().toByteArray()))
                .build();
        return new Request.Builder()
                .url(digaApiRequest.getUrl())
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

}

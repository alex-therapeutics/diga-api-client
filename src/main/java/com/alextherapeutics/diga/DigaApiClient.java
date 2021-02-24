package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaApiRequest;
import com.alextherapeutics.diga.model.DigaApiResponse;
import de.tk.opensource.secon.SeconException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Main entry point to interact with the DiGA API
 * Create using a DigaApiClientSettings object which contains required input
 */
@Slf4j
public class DigaApiClient {
    private DigaApiClientSettings settings;

    private DigaEncryptionFactory encryptionFactory;
    private DigaHttpClient httpClient;
    private DigaCodeParser codeParser;
    private DigaHealthInsuranceDirectory healthInsuranceDirectory;
    private DigaCodeValidationRequestFactory validationRequestFactory;
    private String senderIk;

    /**
     * Create a Diga API client with the default implementation.
     * @param settings
     * @throws DigaApiException
     */
    public DigaApiClient(DigaApiClientSettings settings) throws DigaApiException {
        this.settings = settings;
        init();
    }

    /**
     * Validate a patient's DiGA code.
     * @param digaCode
     * @return
     */
    public DigaApiResponse validateDigaCode(String digaCode) throws DigaApiException {
        try {
            var codeInformation = codeParser.parseCode(digaCode);
            var xmlRequest = validationRequestFactory.createCodeValidationRequest(
                    codeInformation.getFullDigaCode(),
                    codeInformation.getInsuranceCompanyIKNumber()
            );
            var encryptRequestAttempt = encryptionFactory.newEncryption()
                    .encryptionTarget(new ByteArrayInputStream(xmlRequest))
                    .recipientAlias(DigaUtils.ikNumberWithPrefix(codeInformation.getInsuranceCompanyIKNumber()))
                    .build();
            var httpApiRequest = DigaApiRequest.builder()
                    .url(DigaUtils.buildPostDigaEndpoint(codeInformation.getEndpoint()))
                    .senderIK(senderIk)
                    .recipientIK(codeInformation.getInsuranceCompanyIKNumber())
                    .encryptedContent(encryptRequestAttempt.encrypt())
                    .build();
            var encryptedResponse = httpClient.post(httpApiRequest);
            var decryptResponseBodyAttempt = encryptionFactory.newDecryption()
                    .decryptionTarget(new ByteArrayInputStream(encryptedResponse.getEncryptedBody()))
                    .build();
            return DigaApiResponse.builder()
                    .statusCode(encryptedResponse.getStatusCode())
                    .body(
                            IOUtils.toString(decryptResponseBodyAttempt.decrypt().toByteArray(), "UTF-8")
                    )
                    .build();
        } catch (IOException | JAXBException | DigaHttpClientException | SeconException e) {
            log.error("Failed to validate DiGA code", e);
            throw new DigaApiException(e);
        }
    }

    private void init() throws DigaApiException {
        try {
            var privateKeyStoreBytes = IOUtils.toByteArray(settings.getPrivateKeyStoreFile());
            var healthInsurancePublicKeyStoreBytes = IOUtils.toByteArray(settings.getHealthInsurancePublicKeyStoreFile());
            healthInsuranceDirectory = DigaHealthInsuranceDirectory.getInstance(settings.getHealthInsuranceMappingFile());
            encryptionFactory = DigaEncryptionFactory.builder()
                    .privateKeyBytes(privateKeyStoreBytes)
                    .privateKeyAlias(settings.getPrivateKeyAlias())
                    .privateKeyPassword(settings.getPrivateKeyStorePassword())
                    .publicKeysBytes(healthInsurancePublicKeyStoreBytes)
                    .publicKeyDirectoryPassword(settings.getHealthInsurancePublicKeyStorePassword())
                    .build();
            httpClient = DigaHttpClient.builder()
                    .keyStoreFileContent(privateKeyStoreBytes)
                    .certificatesFileContent(healthInsurancePublicKeyStoreBytes)
                    .keyStorePassword(settings.getPrivateKeyStorePassword())
                    .certificatesPassword(settings.getHealthInsurancePublicKeyStorePassword())
                    .build();
            codeParser = new DigaCodeParser(healthInsuranceDirectory);
            validationRequestFactory = DigaCodeValidationRequestFactory.builder()
                    .digaId(settings.getSenderDigaId())
                    .senderIk(settings.getSenderIkNUmber())
                    .build();
            senderIk = settings.getSenderIkNUmber();
        } catch (SeconException | JAXBException | DigaHttpClientException | IOException e) {
            log.error("DigA API client initialization failed", e);
            throw new DigaApiException(e);
        }
    }
}

package com.alextherapeutics.diga;

import com.alextherapeutics.diga.implementation.*;
import com.alextherapeutics.diga.model.*;
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
public final class DigaApiClient {

    private DigaEncryptionFactory encryptionFactory;
    private DigaHttpClient httpClient;
    private DigaCodeParser codeParser;
    private DigaHealthInsuranceDirectory healthInsuranceDirectory;
    private DigaXmlRequestWriter xmlRequestWriter;
    private DigaXmlRequestReader xmlRequestReader;
    private String senderIk;

    /**
     * Create a working Diga API client with default class implementations.
     * @param settings
     * @throws DigaApiException
     */
    public DigaApiClient(DigaApiClientSettings settings) throws DigaApiException {
        init(settings);
    }
    /**
     * Create a DiGA API client with control over which class implementations to use.
     * You must provide an implementation for each interface.
     * Feel free to use the default ones (they have public constructors or builders) in
     * cases where you don't want to write your own.
     */
    // maybe you can have builder.default with default methods to call?
    // builder constr
//    @Builder
//    public DigaApiClient(
//    )

    /**
     * Validate a patient's DiGA code.
     * @param digaCode
     * @return
     */
    public DigaApiResponse validateDigaCode(String digaCode) throws DigaApiException {
        return performCodeValidation(
                codeParser.parseCode(digaCode)
        );
    }

    /**
     * Send a test request to the endpoint of the company of the provided company prefix
     * You can find prefixes in the health insurance company mapping file at https://kkv.gkv-diga.de/
     * @param insuranceCompanyPrefix
     * @return
     */
    public DigaApiResponse sendTestRequest(DigaApiTestCode testCode, String insuranceCompanyPrefix) throws DigaApiException {
        var healthInsuranceInformation = healthInsuranceDirectory.getInformation(insuranceCompanyPrefix);
        var testCodeInformation = DigaCodeInformation.builder()
                .fullDigaCode(testCode.getCode())
                .endpoint(healthInsuranceInformation.getEndpunktKommunikationsstelle())
                .insuranceCompanyIKNumber(healthInsuranceInformation.getIKAbrechnungsstelle())
                .build();
        return performCodeValidation(testCodeInformation);
    }

    private DigaApiResponse performCodeValidation(DigaCodeInformation codeInformation) throws DigaApiException {
        try {
            var xmlRequest = xmlRequestWriter.createCodeValidationRequest(codeInformation);
            var encryptRequestAttempt = encryptionFactory.newEncryption()
                    .encryptionTarget(xmlRequest)
                    .recipientAlias(DigaUtils.ikNumberWithPrefix(codeInformation.getInsuranceCompanyIKNumber()))
                    .build();
            var encryptedXmlBody = encryptRequestAttempt.encrypt().toByteArray();
            var httpApiRequest = DigaApiRequest.builder()
                    .url(DigaUtils.buildPostDigaEndpoint(codeInformation.getEndpoint()))
                    .senderIK(senderIk)
                    .recipientIK(codeInformation.getInsuranceCompanyIKNumber())
                    .encryptedContent(encryptedXmlBody)
                    .build();
            var httpResponse = httpClient.post(httpApiRequest);
            var decryptResponseBodyAttempt = encryptionFactory.newDecryption()
                    .decryptionTarget(httpResponse.getEncryptedBody())
                    .build();
            var response = xmlRequestReader.readCodeValidationResponse(new ByteArrayInputStream(decryptResponseBodyAttempt.decrypt().toByteArray()));
            response.setHttpStatusCode(httpResponse.getStatusCode());
            response.setRawXmlRequestBody(IOUtils.toString(xmlRequest, "UTF-8"));
            response.setRawXmlRequestBodyEncrypted(encryptedXmlBody);
            return response;
        } catch (IOException | JAXBException | DigaHttpClientException | SeconException e) {
            log.error("Failed to validate DiGA code", e);
            throw new DigaApiException(e);
        }
    }

    private void init(DigaApiClientSettings settings) throws DigaApiException {
        try {
            var privateKeyStoreBytes = IOUtils.toByteArray(settings.getPrivateKeyStoreFile());
            var healthInsurancePublicKeyStoreBytes = IOUtils.toByteArray(settings.getHealthInsurancePublicKeyStoreFile());
            healthInsuranceDirectory = DigaHealthInsuranceDirectoryFromXml.getInstance(settings.getHealthInsuranceMappingFile());
            encryptionFactory = DigaSeconEncryptionFactory.builder()
                    .privateKeyBytes(privateKeyStoreBytes)
                    .privateKeyAlias(settings.getPrivateKeyAlias())
                    .privateKeyPassword(settings.getPrivateKeyStorePassword())
                    .publicKeysBytes(healthInsurancePublicKeyStoreBytes)
                    .publicKeyDirectoryPassword(settings.getHealthInsurancePublicKeyStorePassword())
                    .build();
            httpClient = DigaOkHttpClient.builder()
                    .keyStoreFileContent(privateKeyStoreBytes)
                    .certificatesFileContent(healthInsurancePublicKeyStoreBytes)
                    .keyStorePassword(settings.getPrivateKeyStorePassword())
                    .certificatesPassword(settings.getHealthInsurancePublicKeyStorePassword())
                    .build();
            codeParser = new DigaCodeDefaultParser(healthInsuranceDirectory);
            xmlRequestWriter = DigaXmlJaxbRequestWriter.builder()
                    .digaId(settings.getSenderDigaId())
                    .senderIk(settings.getSenderIkNUmber())
                    .build();
            xmlRequestReader = new DigaXmlJaxbRequestReader();
            senderIk = settings.getSenderIkNUmber();
        } catch (SeconException | JAXBException | DigaHttpClientException | IOException e) {
            log.error("DigA API client initialization failed", e);
            throw new DigaApiException(e);
        }
    }
}

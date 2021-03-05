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

package com.alextherapeutics.diga;

import com.alextherapeutics.diga.implementation.*;
import com.alextherapeutics.diga.model.*;
import de.tk.opensource.secon.SeconException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Main entry point to perform code validation and invoicing against the DiGA API.
 *
 * Main methods {@link #validateDigaCode(String)} and {@link #invoiceDiga(DigaInvoice)} return a response object
 * with information on if the request was successful, error information if it wasn't, and information on which request
 * was made including the full XML bodies of the requests and responses for persisting if desired.
 *
 * There is also a test version of each of the main methods for sending test requests.
 *
 * You can create this class in two ways:
 *
 * - Create a client using a {@link DigaApiClientSettings} object, which will create a working client with default
 * class implementations provided by this library. The settings object will contain the input required to set up
 * a working client.
 *
 * - Or you can create a client using the builder {@link DigaApiClientBuilder}, where you will control yourself
 * which class implementations are used. This enables you to write custom implementations for some or all of the
 * interfaces, like for example providing your own {@link DigaHttpClient}. Note that you have to provide a value
 * for each builder property. If you want to use default classes for some values, you can do so, just instantiate
 * them yourself (they are public). Look at the private "initDefault" method in this class for inspiration on how
 * to do that.
 */
@Slf4j
@Builder
@AllArgsConstructor
public final class DigaApiClient {
    @NonNull
    private DigaEncryptionFactory encryptionFactory;
    @NonNull
    private DigaHttpClient httpClient;
    @NonNull
    private DigaCodeParser codeParser;
    @NonNull
    private DigaHealthInsuranceDirectory healthInsuranceDirectory;
    @NonNull
    private DigaXmlRequestWriter xmlRequestWriter;
    @NonNull
    private DigaXmlRequestReader xmlRequestReader;
    @NonNull
    private DigaInformation digaInformation;

    /**
     * Create a working Diga API client with default class implementations.
     * @param settings - required inputs for creating all default class implementations
     * @param digaInformation - static information about your diga and your company used to validate codes and create invoices
     * @throws DigaApiException
     */
    public DigaApiClient(DigaApiClientSettings settings, DigaInformation digaInformation) throws DigaApiException {
        this.digaInformation = digaInformation;
        initDefault(settings);
    }

    /**
     * Attempt to validate a patient's DiGA code against the API.
     * @param digaCode - the full code (16 letters) as a String object.
     * @return a {@link DigaCodeValidationResponse} object containing information on the response from the API.
     */
    public DigaCodeValidationResponse validateDigaCode(String digaCode) throws DigaApiException {
        return performCodeValidation(
                codeParser.parseCodeForValidation(digaCode)
        );
    }

    /**
     * Send an Invoice for a DiGA prescription.
     * @param invoice - individual invoice details
     * @return An object containing details on the response to the invoice request as well as request details such as
     * which insurance company was sent to, which endpoint, IK, etc.
     *
     * The contents of the Invoice itself is located in DigaInvoiceResponse.getRawXmlRequestBody(), you can fetch
     * that and use it for accounting needs.
     *
     * You can check that the invoice request succeeded by checking if (response.hasError()) {}. If hasError is false,
     * the invoice was successful.
     *
     * @throws DigaApiException
     */
    public DigaInvoiceResponse invoiceDiga(DigaInvoice invoice) throws DigaApiException {
        var billingInformation = codeParser.parseCodeForBilling(invoice.getValidatedDigaCode());
        return performDigaInvoicing(invoice, billingInformation, DigaProcessCode.BILLING);
    }

    /**
     * Send a test request to the endpoint of the company of the provided company prefix
     * @param testCode - one of the specified test codes
     * @param insuranceCompanyPrefix - the prefix of the company to send the request to (according to the mapping file at https://kkv.gkv-diga.de/)
     * @return
     * @throws DigaApiException
     */
    public DigaCodeValidationResponse sendTestCodeValidationRequest(DigaApiTestCode testCode, String insuranceCompanyPrefix) throws DigaApiException {
        var healthInsuranceInformation = healthInsuranceDirectory.getInformation(insuranceCompanyPrefix);
        var testCodeInformation = DigaCodeInformation.builder()
                .fullDigaCode(testCode.getCode())
                .endpoint(healthInsuranceInformation.getEndpunktKommunikationsstelle())
                .insuranceCompanyIKNumber(healthInsuranceInformation.getIKAbrechnungsstelle())
                .insuranceCompanyName(healthInsuranceInformation.getNameDesKostentraegers())
                .build();
        return performCodeValidation(testCodeInformation);
    }

    /**
     * Send a test invoice to the specified insurance company.
     * Note that currently the DiGA APIs do not seem to respond with 'valid' to test requests, even using valid test codes.
     * If you are successful, you will receive a response which validated all the XML schemas, but it has an error like
     * "could not find the code".
     *
     * @param invoice - the invoice to send
     * @param insuranceCompanyPrefix - the prefix of the company to send it to, as listed in the insurance company mapping file
     * @return An object containing details on the response as well as the request details. See 'invoiceDiga' method.
     */
    public DigaInvoiceResponse sendTestInvoiceRequest(DigaInvoice invoice, String insuranceCompanyPrefix) throws DigaApiException {
        var healthInsuranceInformation = healthInsuranceDirectory.getInformation(insuranceCompanyPrefix);
        var billingInformation = DigaBillingInformation.builder()
                .endpoint(healthInsuranceInformation.getEndpunktKommunikationsstelle())
                .insuranceCompanyIKNumber(healthInsuranceInformation.getIKAbrechnungsstelle())
                .buyerCompanyCreditorIk(healthInsuranceInformation.getIKDesRechnungsempfaengers())
                .buyerCompanyId(healthInsuranceInformation.getNameDesKostentraegers())
                .insuranceCompanyName(healthInsuranceInformation.getNameDesKostentraegers())
                .buyerCompanyPostalCode(healthInsuranceInformation.getPLZ())
                .buyerCompanyAddressLine(healthInsuranceInformation.getStrassePostfach() + " " + healthInsuranceInformation.getHausnummerPostfachnummer())
                .buyerCompanyCity(healthInsuranceInformation.getOrt())
                .build();
        return performDigaInvoicing(invoice, billingInformation, DigaProcessCode.BILLING_TEST);
    }

    private DigaCodeValidationResponse performCodeValidation(DigaCodeInformation codeInformation) throws DigaApiException {
        try {
            var xmlRequest = xmlRequestWriter.createCodeValidationRequest(codeInformation);
            var encryptRequestAttempt = encryptionFactory.newEncryption()
                    .encryptionTarget(xmlRequest)
                    .recipientAlias(DigaUtils.ikNumberWithPrefix(codeInformation.getInsuranceCompanyIKNumber()))
                    .build();
            var encryptedXmlBody = encryptRequestAttempt.encrypt().toByteArray();
            var httpApiRequest = DigaApiHttpRequest.builder()
                    .url(DigaUtils.buildPostDigaEndpoint(codeInformation.getEndpoint()))
                    .senderIK(digaInformation.getManufacturingCompanyIk())
                    .recipientIK(codeInformation.getInsuranceCompanyIKNumber())
                    .encryptedContent(encryptedXmlBody)
                    .processCode(
                            DigaUtils.isDigaTestCode(codeInformation.getFullDigaCode())
                                    ? DigaProcessCode.CODE_VALIDATION_TEST
                                    : DigaProcessCode.CODE_VALIDATION
                    )
                    .build();
            var httpResponse = httpClient.post(httpApiRequest);
            var decryptResponseBodyAttempt = encryptionFactory.newDecryption()
                    .decryptionTarget(httpResponse.getEncryptedBody())
                    .build();
            var response = xmlRequestReader.readCodeValidationResponse(new ByteArrayInputStream(decryptResponseBodyAttempt.decrypt().toByteArray()));
            response.setHttpStatusCode(httpResponse.getStatusCode());
            response.setRawXmlRequestBody(IOUtils.toString(xmlRequest, "UTF-8"));
            response.setRawXmlRequestBodyEncrypted(encryptedXmlBody);
            addReceiverDetailsToResponse(response, codeInformation);
            return response;
        } catch (IOException | JAXBException | DigaHttpClientException | SeconException e) {
            // TODO fix catch all here, see issue #14
            log.error("Failed to validate DiGA code", e);
            throw new DigaApiException(e);
        }
    }

    private DigaInvoiceResponse performDigaInvoicing(DigaInvoice invoice, DigaBillingInformation billingInformation, DigaProcessCode processCode) throws DigaApiException {
        try {
            var xmlInvoice = xmlRequestWriter.createBillingRequest(invoice, billingInformation);
            var encryptionAttempt = encryptionFactory.newEncryption()
                    .encryptionTarget(xmlInvoice)
                    .recipientAlias(DigaUtils.ikNumberWithPrefix(billingInformation.getInsuranceCompanyIKNumber()))
                    .build();
            var encryptedXmlInvoice = encryptionAttempt.encrypt().toByteArray();
            var httpApiRequest = DigaApiHttpRequest.builder()
                    .encryptedContent(encryptedXmlInvoice)
                    .recipientIK(billingInformation.getInsuranceCompanyIKNumber())
                    .processCode(processCode)
                    .url(DigaUtils.buildPostDigaEndpoint(billingInformation.getEndpoint()))
                    .senderIK(digaInformation.getManufacturingCompanyIk())
                    .build();
            var httpResponse = httpClient.post(httpApiRequest);
            var decryptAttempt = encryptionFactory.newDecryption()
                    .decryptionTarget(httpResponse.getEncryptedBody())
                    .build();
            var decrypted = decryptAttempt.decrypt().toByteArray();
            var response = xmlRequestReader.readBillingReport(new ByteArrayInputStream(decrypted));
            response.setHttpStatusCode(httpResponse.getStatusCode());
            response.setRawXmlRequestBody(IOUtils.toString(xmlInvoice, "UTF-8"));
            response.setRawXmlRequestBodyEncrypted(encryptedXmlInvoice);
            addReceiverDetailsToResponse(response, billingInformation);
            return response;
        } catch (IOException | JAXBException | DigaHttpClientException | SeconException e) {
            // TODO fix catch all here, see issue #14
            log.error("Failed to invoice DiGA", e);
            throw new DigaApiException(e);
        }
    }

    private void addReceiverDetailsToResponse(AbstractDigaApiResponse response, AbstractDigaInsuranceInformation insuranceInformation) {
        response.setReceivingInsuranceCompanyEndpoint(insuranceInformation.getEndpoint());
        response.setReceivingInsuranceCompanyIk(insuranceInformation.getInsuranceCompanyIKNumber());
        response.setReceivingInsuranceCompanyName(insuranceInformation.getInsuranceCompanyName());
    }

    private void initDefault(DigaApiClientSettings settings) throws DigaApiException {
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
                    .digaInformation(digaInformation)
                    .build();
            xmlRequestReader = new DigaXmlJaxbRequestReader();
        } catch (SeconException | JAXBException | DigaHttpClientException | IOException e) {
            log.error("DigA API client initialization failed", e);
            throw new DigaApiException(e);
        }
    }
}

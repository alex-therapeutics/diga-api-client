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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import javax.xml.bind.JAXBException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

/**
 * Main entry point to perform code validation and invoicing against the DiGA API.
 *
 * <p>Main methods {@link #validateDigaCode(String)} and {@link #invoiceDiga(DigaInvoice)} return a
 * response object with information on if the request was successful, error information if it
 * wasn't, and information on which request was made including the full XML bodies of the requests
 * and responses for persisting if desired.
 *
 * <p>There is also a test version of each of the main methods for sending test requests.
 *
 * <p>You can create this class in two ways:
 *
 * <p>- Create a client using a {@link DigaApiClientSettings} object, which will create a working
 * client with default class implementations provided by this library. The settings object will
 * contain the input required to set up a working client.
 *
 * <p>- Or you can create a client using the builder {@link DigaApiClientBuilder}, where you will
 * control yourself which class implementations are used. This enables you to write custom
 * implementations for some or all of the interfaces, like for example providing your own {@link
 * DigaHttpClient}. Note that you have to provide a value for each builder property. If you want to
 * use default classes for some values, you can do so, just instantiate them yourself (they are
 * public). Look at the private "initDefault" method in this class for inspiration on how to do
 * that.
 */
@Slf4j
@Builder
@AllArgsConstructor
public final class DigaApiClient {
  @NonNull private final DigaInformation digaInformation;
  @NonNull private DigaEncryptionFactory encryptionFactory;
  @NonNull private DigaHttpClient httpClient;
  @NonNull private DigaCodeParser codeParser;
  @NonNull private DigaHealthInsuranceDirectory healthInsuranceDirectory;
  @NonNull private DigaXmlRequestWriter xmlRequestWriter;
  @NonNull private DigaXmlRequestReader xmlRequestReader;

  /**
   * Create a working Diga API client with default class implementations.
   *
   * @param settings - required inputs for creating all default class implementations
   * @param digaInformation - static information about your diga and your company used to validate
   *     codes and create invoices
   * @throws DigaApiException
   */
  public DigaApiClient(DigaApiClientSettings settings, DigaInformation digaInformation)
      throws DigaApiException {
    this.digaInformation = digaInformation;
    initDefault(settings);
  }

  /**
   * Attempt to validate a patient's DiGA code against the API.
   *
   * @param digaCode - the full code (16 letters) as a String object.
   * @return a {@link DigaCodeValidationResponse} object containing information on the response from
   *     the API. This response may contain errors, in which case there are error messages in the
   *     response as well as the raw XML request that was sent which you can access for debugging
   *     purposes or to save failed requests for retrying later.
   * @throws DigaCodeValidationException if given an invalid DiGA code
   * @throws DigaXmlWriterException if we fail to create the XML request body to send to the API
   */
  public DigaCodeValidationResponse validateDigaCode(String digaCode)
      throws DigaXmlWriterException, DigaCodeValidationException {
    if (DigaUtils.isDigaTestCode(digaCode)) {
      log.error("A test code was entered: {}", digaCode);
      throw new DigaCodeValidationException("A test code was entered");
    }
    return performCodeValidation(codeParser.parseCodeForValidation(digaCode));
  }

  /**
   * Send an Invoice for a DiGA prescription.
   *
   * @param invoice - individual invoice details
   * @return An object containing details on the response to the invoice request, the generated
   *     invoice itself, as well as request details such as which insurance company was sent to,
   *     which endpoint, IK, etc. This response may contain errors, in which case there are error
   *     messages in the object.
   *     <p>You can check that the invoice request succeeded by checking if (response.hasError())
   *     {}. If hasError is false, the invoice was successful.
   *     <p>You should check {@link DigaInvoiceResponse#isRequiresManualAction()} to see whether the
   *     invoice needs to be handled manually, in the cases when the target insurance company does
   *     not support sending invoices via the API.
   *     <p>The contents of the Invoice itself is located in {@link
   *     DigaInvoiceResponse#getGeneratedInvoice()}, you can fetch that and use it for accounting
   *     needs both when invoicing was successful and when it was not.
   * @throws DigaCodeValidationException if given an invalid DiGA code
   * @throws DigaXmlWriterException if we fail to create the XML request body (the XRechnung
   *     invoice)
   */
  public DigaInvoiceResponse invoiceDiga(DigaInvoice invoice)
      throws DigaCodeValidationException, DigaXmlWriterException {
    var billingInformation = codeParser.parseCodeForBilling(invoice.getValidatedDigaCode());
    return performDigaInvoicing(invoice, billingInformation, DigaProcessCode.BILLING);
  }

  /**
   * Send a test request to the endpoint of the company of the provided company prefix
   *
   * @param testCode - one of the specified test codes
   * @param insuranceCompanyPrefix - the prefix of the company to send the request to (according to
   *     the mapping file at https://kkv.gkv-diga.de/)
   * @return An object containing details on the response from the API, including errors of such
   *     occured.
   * @throws DigaXmlWriterException if we fail to create the XML request body to send to the API
   */
  public DigaCodeValidationResponse sendTestCodeValidationRequest(
      DigaApiTestCode testCode, String insuranceCompanyPrefix) throws DigaXmlWriterException {
    var healthInsuranceInformation =
        healthInsuranceDirectory.getInformation(insuranceCompanyPrefix);
    var testCodeInformation =
        DigaCodeInformation.builder()
            .fullDigaCode(testCode.getCode())
            .endpoint(healthInsuranceInformation.getEndpunktKommunikationsstelle())
            .insuranceCompanyIKNumber(healthInsuranceInformation.getKostentraegerkennung())
            .clearingCenterIKNumber(healthInsuranceInformation.getIKAbrechnungsstelle())
            .insuranceCompanyName(healthInsuranceInformation.getNameDesKostentraegers())
            .build();
    return performCodeValidation(testCodeInformation);
  }

  /**
   * Send a test invoice to the specified insurance company. Note that currently the DiGA APIs do
   * not seem to respond with 'valid' to test requests, even using valid test codes. If you are
   * successful, you will receive a response which validated all the XML schemas, but it has an
   * error like "could not find the code".
   *
   * @param invoice - the invoice to send
   * @param insuranceCompanyPrefix - the prefix of the company to send it to, as listed in the
   *     insurance company mapping file
   * @return An object containing details on the response as well as the request details. See
   *     'invoiceDiga' method.
   * @throws DigaXmlWriterException if we fail to create the XML request body (the XRechnung
   *     invoice)
   */
  public DigaInvoiceResponse sendTestInvoiceRequest(
      DigaInvoice invoice, String insuranceCompanyPrefix) throws DigaXmlWriterException {
    var healthInsuranceInformation =
        healthInsuranceDirectory.getInformation(insuranceCompanyPrefix);
    var billingInformation =
        DigaBillingInformation.builder()
            .endpoint(healthInsuranceInformation.getEndpunktKommunikationsstelle())
            .insuranceCompanyIKNumber(healthInsuranceInformation.getKostentraegerkennung())
            .clearingCenterIKNumber(healthInsuranceInformation.getIKAbrechnungsstelle())
            .buyerCompanyCreditorIk(healthInsuranceInformation.getIKDesRechnungsempfaengers())
            .insuranceCompanyName(healthInsuranceInformation.getNameDesKostentraegers())
            .buyerCompanyPostalCode(
                healthInsuranceInformation.getPLZ() != null
                    ? healthInsuranceInformation.getPLZ()
                    : DigaBillingInformation.INFORMATION_MISSING)
            .buyerCompanyAddressLine(
                healthInsuranceInformation.getStrassePostfach() != null
                    ? healthInsuranceInformation.getStrassePostfach()
                        + " "
                        + healthInsuranceInformation.getHausnummerPostfachnummer()
                    : DigaBillingInformation.INFORMATION_MISSING)
            .buyerCompanyCity(
                healthInsuranceInformation.getOrt() != null
                    ? healthInsuranceInformation.getOrt()
                    : DigaBillingInformation.INFORMATION_MISSING)
            .buyerInvoicingMethod(
                DigaInvoiceMethod.fromIdentifier(
                    healthInsuranceInformation.getVersandart().intValue()))
            .buyerInvoicingEmail(healthInsuranceInformation.getEMailKostentraeger())
            .build();
    return performDigaInvoicing(invoice, billingInformation, DigaProcessCode.BILLING_TEST);
  }

  private DigaCodeValidationResponse performCodeValidation(DigaCodeInformation codeInformation)
      throws DigaXmlWriterException {
    var xmlRequest = xmlRequestWriter.createCodeValidationRequest(codeInformation);
    var encryptRequestAttempt =
        encryptionFactory
            .newEncryption()
            .encryptionTarget(xmlRequest)
            .recipientAlias(
                DigaUtils.ikNumberWithPrefix(codeInformation.getClearingCenterIKNumber()))
            .build();
    try {
      var encryptedXmlBody = encryptRequestAttempt.encrypt().toByteArray();
      var httpApiRequest =
          DigaApiHttpRequest.builder()
              .url(DigaUtils.buildPostDigaEndpoint(codeInformation.getEndpoint()))
              .senderIK(digaInformation.getManufacturingCompanyIk())
              .recipientIK(codeInformation.getClearingCenterIKNumber())
              .encryptedContent(encryptedXmlBody)
              .processCode(
                  DigaUtils.isDigaTestCode(codeInformation.getFullDigaCode())
                      ? DigaProcessCode.CODE_VALIDATION_TEST
                      : DigaProcessCode.CODE_VALIDATION)
              .build();
      var httpResponse = httpClient.post(httpApiRequest);
      var decryptResponseBodyAttempt =
          encryptionFactory
              .newDecryption()
              .decryptionTarget(httpResponse.getEncryptedBody())
              .build();
      var response =
          xmlRequestReader.readCodeValidationResponse(
              new ByteArrayInputStream(decryptResponseBodyAttempt.decrypt().toByteArray()));
      response.setHttpStatusCode(httpResponse.getStatusCode());
      response.setRawXmlRequestBody(xmlRequest);
      response.setRawXmlRequestBodyEncrypted(encryptedXmlBody);
      addReceiverDetailsToResponse(response, codeInformation);
      return response;
    } catch (DigaHttpClientException
        | DigaEncryptionException
        | DigaDecryptionException
        | DigaXmlReaderException e) {
      log.error("Failed to validate DiGA code {}", codeInformation.getFullDigaCode(), e);
      return buildCodeValidationResponseFromException(xmlRequest, e, codeInformation);
    }
  }

  private DigaInvoiceResponse performDigaInvoicing(
      DigaInvoice invoice, DigaBillingInformation billingInformation, DigaProcessCode processCode)
      throws DigaXmlWriterException {
    var xmlInvoice = xmlRequestWriter.createBillingRequest(invoice, billingInformation);
    if (!billingInformation.getBuyerInvoicingMethod().equals(DigaInvoiceMethod.API)) {
      return buildManualInvoicingResponse(billingInformation, xmlInvoice);
    }
    try {
      return performDigaInvoicingAgainstApi(billingInformation, xmlInvoice, processCode);
    } catch (DigaHttpClientException
        | DigaDecryptionException
        | DigaEncryptionException
        | DigaXmlReaderException e) {
      log.error(
          "Failed to invoice DiGA API for invoice id {}, code {}",
          invoice.getInvoiceId(),
          invoice.getValidatedDigaCode(),
          e);
      return buildInvoiceResponseFromException(xmlInvoice, e, billingInformation);
    }
  }

  private DigaInvoiceResponse performDigaInvoicingAgainstApi(
      DigaBillingInformation billingInformation, byte[] xmlInvoice, DigaProcessCode processCode)
      throws DigaEncryptionException,
          DigaHttpClientException,
          DigaDecryptionException,
          DigaXmlReaderException {
    var encryptionAttempt =
        encryptionFactory
            .newEncryption()
            .encryptionTarget(xmlInvoice)
            .recipientAlias(
                DigaUtils.ikNumberWithPrefix(billingInformation.getClearingCenterIKNumber()))
            .build();
    var encryptedXmlInvoice = encryptionAttempt.encrypt().toByteArray();
    var httpApiRequest =
        DigaApiHttpRequest.builder()
            .encryptedContent(encryptedXmlInvoice)
            .recipientIK(billingInformation.getClearingCenterIKNumber())
            .processCode(processCode)
            .url(DigaUtils.buildPostDigaEndpoint(billingInformation.getEndpoint()))
            .senderIK(digaInformation.getManufacturingCompanyIk())
            .build();
    var httpResponse = httpClient.post(httpApiRequest);
    var decryptAttempt =
        encryptionFactory.newDecryption().decryptionTarget(httpResponse.getEncryptedBody()).build();
    var decrypted = decryptAttempt.decrypt().toByteArray();
    var response = xmlRequestReader.readBillingReport(new ByteArrayInputStream(decrypted));
    response.setHttpStatusCode(httpResponse.getStatusCode());
    response.setRawXmlRequestBody(xmlInvoice);
    response.setGeneratedInvoice(IOUtils.toString(xmlInvoice, "UTF-8"));
    response.setRawXmlRequestBodyEncrypted(encryptedXmlInvoice);
    addReceiverDetailsToResponse(response, billingInformation);
    return response;
  }

  private DigaInvoiceResponse buildManualInvoicingResponse(
      DigaBillingInformation billingInformation, byte[] xmlInvoice) {
    return DigaInvoiceResponse.builder()
        .requiresManualAction(true)
        .invoiceMethod(billingInformation.getBuyerInvoicingMethod())
        .rawXmlRequestBody(xmlInvoice)
        .generatedInvoice(IOUtils.toString(xmlInvoice, "UTF-8"))
        .insuranceCompanyInvoiceEmail(billingInformation.getBuyerInvoicingEmail())
        .receivingInsuranceCompanyEndpoint(billingInformation.getEndpoint())
        .receivingInsuranceCompanyIk(billingInformation.getInsuranceCompanyIKNumber())
        .receivingInsuranceCompanyName(billingInformation.getInsuranceCompanyName())
        .build();
  }

  private void addReceiverDetailsToResponse(
      AbstractDigaApiResponse response, AbstractDigaInsuranceInformation insuranceInformation) {
    response.setReceivingInsuranceCompanyEndpoint(insuranceInformation.getEndpoint());
    response.setReceivingInsuranceCompanyIk(insuranceInformation.getInsuranceCompanyIKNumber());
    response.setReceivingInsuranceCompanyName(insuranceInformation.getInsuranceCompanyName());
  }

  private void initDefault(DigaApiClientSettings settings) throws DigaApiException {
    try {
      var privateKeyStoreBytes = IOUtils.toByteArray(settings.getPrivateKeyStoreFile());
      var healthInsurancePublicKeyStoreBytes =
          IOUtils.toByteArray(settings.getHealthInsurancePublicKeyStoreFile());
      healthInsuranceDirectory =
          DigaHealthInsuranceDirectoryFromXml.getInstance(settings.getHealthInsuranceMappingFile());
      encryptionFactory =
          DigaSeconEncryptionFactory.builder()
              .privateKeyBytes(privateKeyStoreBytes)
              .privateKeyAlias(settings.getPrivateKeyAlias())
              .privateKeyPassword(settings.getPrivateKeyStorePassword())
              .publicKeysBytes(healthInsurancePublicKeyStoreBytes)
              .publicKeyDirectoryPassword(settings.getHealthInsurancePublicKeyStorePassword())
              .build();
      httpClient =
          DigaOkHttpClient.builder()
              .keyStoreFileContent(privateKeyStoreBytes)
              .certificatesFileContent(healthInsurancePublicKeyStoreBytes)
              .keyStorePassword(settings.getPrivateKeyStorePassword())
              .certificatesPassword(settings.getHealthInsurancePublicKeyStorePassword())
              .build();
      codeParser = new DigaCodeDefaultParser(healthInsuranceDirectory);
      xmlRequestWriter =
          DigaXmlJaxbRequestWriter.builder().digaInformation(digaInformation).build();
      xmlRequestReader = new DigaXmlJaxbRequestReader();
    } catch (SeconException | JAXBException | DigaHttpClientException | IOException e) {
      log.error("DigA API client initialization failed", e);
      throw new DigaApiException(e);
    }
  }

  private DigaCodeValidationResponse buildCodeValidationResponseFromException(
      byte[] xmlRequest, Throwable error, DigaCodeInformation information) {
    var response =
        DigaCodeValidationResponse.builder()
            .hasError(true)
            .errors(Collections.singletonList(new DigaApiExceptionError(error)))
            .rawXmlRequestBody(xmlRequest)
            .build();
    addReceiverDetailsToResponse(response, information);
    return response;
  }

  private DigaInvoiceResponse buildInvoiceResponseFromException(
      byte[] xmlRequest, Throwable error, DigaBillingInformation information) {
    var response =
        DigaInvoiceResponse.builder()
            .hasError(true)
            .errors(Collections.singletonList(new DigaApiExceptionError(error)))
            .rawXmlRequestBody(xmlRequest)
            .generatedInvoice(IOUtils.toString(xmlRequest, "UTF-8"))
            .build();
    addReceiverDetailsToResponse(response, information);
    return response;
  }
}

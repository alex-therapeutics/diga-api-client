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

import com.alextherapeutics.diga.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

class DigaApiClientTest {
    private DigaApiClient client;
    private DigaEncryptionFactory encryptionFactory;
    private DigaHttpClient httpClient;
    private DigaCodeParser codeParser;
    private DigaHealthInsuranceDirectory healthInsuranceDirectory;
    private DigaXmlRequestWriter xmlRequestWriter;
    private DigaXmlRequestReader xmlRequestReader;
    private DigaInformation digaInformation;

    @BeforeEach
    void setUp() {
        digaInformation = DigaInformation.builder()
                        .digaId("12345")
                        .digaName("MyDiga")
                        .manufacturingCompanyName("My Diga Company")
                        .manufacturingCompanyIk("123456789")
                        .netPricePerPrescription(new BigDecimal(100))
                        .applicableVATpercent(new BigDecimal(19))
                        .manufacturingCompanyVATRegistration("DE 123 456")
                        .contactPersonForBilling(
                                DigaInformation.ContactPersonForBilling.builder()
                                        .fullName("Sven Svensson")
                                        .phoneNumber("+46 70 123 45 67")
                                        .emailAddress("diga@diga.de")
                                        .build()
                        )
                        .companyTradeAddress(
                                DigaInformation.CompanyTradeAddress.builder()
                                        .adressLine("Test Street 1")
                                        .postalCode("123 45")
                                        .city("Stockholm")
                                        .countryCode("SE")
                                        .build()
                        )
                        .build();
        encryptionFactory = Mockito.mock(DigaEncryptionFactory.class);
        httpClient = Mockito.mock(DigaHttpClient.class);
        codeParser = Mockito.mock(DigaCodeParser.class);
        healthInsuranceDirectory = Mockito.mock(DigaHealthInsuranceDirectory.class);
        xmlRequestWriter = Mockito.mock(DigaXmlRequestWriter.class);
        xmlRequestReader = Mockito.mock(DigaXmlRequestReader.class);

        client = DigaApiClient.builder()
                .httpClient(httpClient)
                .xmlRequestReader(xmlRequestReader)
                .encryptionFactory(encryptionFactory)
                .healthInsuranceDirectory(healthInsuranceDirectory)
                .codeParser(codeParser)
                .digaInformation(digaInformation)
                .xmlRequestWriter(xmlRequestWriter)
                .build();
    }

    @Test
    void testValidateDigaCodeDoesntAcceptTestCodes() {
        Assertions.assertThrows(
                DigaCodeValidationException.class,
                () -> client.validateDigaCode(DigaApiTestCode.ERROR_CODE_NOT_FOUND.getCode())
        );
    }
    @Test
    void testCodeParsingFailureRethrows() throws DigaCodeValidationException {
        var code = "wrong";
        Mockito.when(codeParser.parseCodeForValidation(code)).thenThrow(DigaCodeValidationException.class);
        Assertions.assertThrows(
                DigaCodeValidationException.class,
                () -> client.validateDigaCode(code)
        );
    }
    @Test
    void testXmlWritingFailureRethrows() throws DigaXmlWriterException {
        Mockito.when(xmlRequestWriter.createCodeValidationRequest(Mockito.any())).thenThrow(DigaXmlWriterException.class);
        Assertions.assertThrows(
                DigaXmlWriterException.class,
                () -> client.validateDigaCode("hi")
        );
    }
    @Test
    void testExceptionAfterXmlWritingReturnsResponseCodeValidation() throws DigaEncryptionException, DigaXmlWriterException, DigaCodeValidationException {
        var codeInfo = Mockito.mock(DigaCodeInformation.class);
        Mockito.when(codeParser.parseCodeForValidation(Mockito.anyString())).thenReturn(codeInfo);
        Mockito.when(codeInfo.getInsuranceCompanyIKNumber()).thenReturn("IK123456789");
        var xmlRequest = new byte[]{5,6};
        Mockito.when(xmlRequestWriter.createCodeValidationRequest(Mockito.any())).thenReturn(xmlRequest);
        var encrBuild = Mockito.mock(DigaEncryption.DigaEncryptionBuilder.class, Mockito.RETURNS_SELF);
        var encr = Mockito.mock(DigaEncryption.class);
        Mockito.when(encryptionFactory.newEncryption()).thenReturn(encrBuild);
        Mockito.when(encrBuild.build()).thenReturn(encr);
        Mockito.when(encr.encrypt()).thenThrow(DigaEncryptionException.class);

        var resp = client.validateDigaCode("my-code");
        Assertions.assertTrue(resp.isHasError());
        Assertions.assertTrue(resp.getErrors().size() > 0);
        Assertions.assertNotNull(resp.getErrors().get(0).getError());
        Assertions.assertArrayEquals(xmlRequest, resp.getRawXmlRequestBody());
    }
    @Test
    void testExceptionAfterXmlWritingReturnsResponseBilling() throws DigaEncryptionException, DigaXmlWriterException, DigaCodeValidationException {
        var invoice = DigaInvoice.builder().invoiceId("1").validatedDigaCode("code").digavEid("12345000").build();
        var info = Mockito.mock(DigaBillingInformation.class);
        Mockito.when(codeParser.parseCodeForBilling(Mockito.anyString())).thenReturn(info);
        Mockito.when(info.getInsuranceCompanyIKNumber()).thenReturn("IK123456789");
        var xmlRequest = new byte[]{5,6};
        Mockito.when(xmlRequestWriter.createBillingRequest(invoice, info)).thenReturn(xmlRequest);
        var encrBuild = Mockito.mock(DigaEncryption.DigaEncryptionBuilder.class, Mockito.RETURNS_SELF);
        var encr = Mockito.mock(DigaEncryption.class);
        Mockito.when(encryptionFactory.newEncryption()).thenReturn(encrBuild);
        Mockito.when(encrBuild.build()).thenReturn(encr);
        Mockito.when(encr.encrypt()).thenThrow(DigaEncryptionException.class);

        var resp = client.invoiceDiga(invoice);
        Assertions.assertTrue(resp.isHasError());
        Assertions.assertTrue(resp.getErrors().size() > 0);
        Assertions.assertNotNull(resp.getErrors().get(0).getError());
        Assertions.assertArrayEquals(xmlRequest, resp.getRawXmlRequestBody());
    }
}
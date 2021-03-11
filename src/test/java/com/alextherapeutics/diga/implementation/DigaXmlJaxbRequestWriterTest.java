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

import com.alextherapeutics.diga.DigaXmlWriterException;
import com.alextherapeutics.diga.model.DigaBillingInformation;
import com.alextherapeutics.diga.model.DigaCodeInformation;
import com.alextherapeutics.diga.model.DigaInformation;
import com.alextherapeutics.diga.model.DigaInvoice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;

class DigaXmlJaxbRequestWriterTest {
    private DigaXmlJaxbRequestWriter writer;

    @BeforeEach
    void init() throws JAXBException {
        writer = new DigaXmlJaxbRequestWriter(
                DigaInformation.builder()
                        .companyTradeAddress(
                                DigaInformation.CompanyTradeAddress.builder()
                                        .countryCode("DE")
                                        .city("dum")
                                        .postalCode("dum")
                                        .adressLine("dum")
                                        .build()
                        )
                        .contactPersonForBilling(
                                DigaInformation.ContactPersonForBilling.builder()
                                        .emailAddress("dum")
                                        .phoneNumber("dum")
                                        .fullName("dum")
                                        .build())
                        .manufacturingCompanyVATRegistration("dum")
                        .applicableVATpercent(new BigDecimal(10))
                        .netPricePerPrescription(new BigDecimal(5000))
                        .manufacturingCompanyIk("dum")
                        .digaName("dum")
                        .digaId("dum")
                        .manufacturingCompanyName("dum")
                        .digaDescription("dum")
                        .build()

        );
    }

    @Test
    void testCreateCodeValidationRequestCompletesWithoutExceptions() throws JAXBException, IOException, DigaXmlWriterException {
        var res = writer.createCodeValidationRequest(
                DigaCodeInformation.builder()
                        .fullDigaCode("dum")
                        .insuranceCompanyName("dum")
                        .insuranceCompanyIKNumber("dum")
                        .endpoint("dum")
                        .personalDigaCode("dum")
                        .build()
        );
        Assertions.assertNotNull(res);
    }

    @Test
    void testCreateBillingRequestCompletesWithoutExceptions() throws JAXBException, IOException, DigaXmlWriterException {
        var res = writer.createBillingRequest(
                DigaInvoice.builder()
                        .validatedDigaCode("dum")
                        .invoiceId("ID1")
                        .build(),
                DigaBillingInformation.builder()
                        .insuranceCompanyName("dum")
                        .insuranceCompanyIKNumber("dum")
                        .endpoint("dum")
                        .buyerCompanyCity("dum")
                        .buyerCompanyPostalCode("dum")
                        .buyerCompanyAddressLine("dum")
                        .buyerCompanyCreditorIk("dum")
                        .build()
        );
        Assertions.assertNotNull(res);
    }
}
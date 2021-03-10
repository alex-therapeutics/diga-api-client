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

import com.alextherapeutics.diga.model.DigaCodeValidationResponse;
import com.alextherapeutics.diga.model.DigaInvoiceResponse;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads decrypted raw XML request/response bodies and outputs more readable Java objects.
 */
public interface DigaXmlRequestReader {
    /**
     * Read an inputstream with XML contents containing the validation of a DiGA code validation request and parse it.
     *
     * @param decryptedResponse
     * @return Relevant information in a new {@link DigaCodeValidationResponse} object. Note that the DigaApiResponse object contains fields which are not set by XML readers, like the HTTP status code.
     * @throws JAXBException
     * @throws IOException
     */
    DigaCodeValidationResponse readCodeValidationResponse(InputStream decryptedResponse) throws JAXBException, IOException;

    /**
     * Read an inputstream with XML contents containing the validation report from a DiGA invoice and parse it.
     *
     * @param decryptedReport
     * @return Relevant information in a new {@link DigaInvoiceResponse} object.
     * @throws JAXBException
     * @throws IOException
     */
    DigaInvoiceResponse readBillingReport(InputStream decryptedReport) throws JAXBException, IOException;
}

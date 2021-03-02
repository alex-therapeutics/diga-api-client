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

import com.alextherapeutics.diga.DigaXmlRequestReader;
import com.alextherapeutics.diga.model.*;
import com.alextherapeutics.diga.model.xml.NachrichtentypStp;
import com.alextherapeutics.diga.model.xml.PruefungFreischaltcode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A XML reader using JAXB.
 */
@Slf4j
public class DigaXmlJaxbRequestReader implements DigaXmlRequestReader {
    private JAXBContext context;
    private Unmarshaller unmarshaller;

    public DigaXmlJaxbRequestReader() throws JAXBException {
        context = JAXBContext.newInstance(PruefungFreischaltcode.class);
        unmarshaller = context.createUnmarshaller();
    }

    @Override
    public DigaApiResponse readCodeValidationResponse(InputStream decryptedResponse) throws JAXBException, IOException {
        var bytes = decryptedResponse.readAllBytes();
        var response = (PruefungFreischaltcode) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));
        validateCodeValidationResponse(response);

        // appendix 4 at https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp
        // seems to differ from the current xsd schema definition. watch for changes here
        return DigaApiResponse.builder()
                .rawXmlResponseBody(IOUtils.toString(bytes, "UTF-8"))
                .hasError(response.getNachrichtentyp().equals(NachrichtentypStp.FEH))
                .errors(getErrors(response))
                .validatedDigaCode(response.getAntwort() == null ? null : response.getAntwort().getFreischaltcode())
                .dayOfServiceProvision(response.getAntwort() == null ? null : response.getAntwort().getTagDerLeistungserbringung().toGregorianCalendar().getTime())
                .prescriptionType(
                        response.getAntwort() == null
                                ? null
                                : DigaPrescriptionType.fromIdentifier(response.getAntwort().getDiGAVEID().substring(5))
                )
                .build();
    }
    private List<DigaApiResponseError> getErrors(PruefungFreischaltcode request) {
        var errors = request.getFehlerinformation();
        return errors == null
                ? Collections.emptyList()
                : errors.stream()
                .map(fehlerinformation -> new DigaApiResponseError(DigaErrorCode.fromCode(fehlerinformation.getFehlernummer().intValue()), fehlerinformation.getFehlertext()))
                .collect(Collectors.toList());
    }

    // log errors if the response looks strange, for manual inspection (it is difficult to know what can go wrong at this point)
    // dont throw exception because the process may work anyway if we are lucky
    private void validateCodeValidationResponse(PruefungFreischaltcode response) {
        if (response.getNachrichtentyp().equals(NachrichtentypStp.ANF)) {
            log.error("Received ANF (request) type in a response from the DiGA API. Should be ANT or FEH. Response: {}", response.toString());
        }
        if (!response.getVersion().equals(DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_VERSION.getValue())) {
            log.error(
                    "Received code validation response with version mismatch. Supported version: {), Response version: {}",
                    DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_VERSION.getValue(),
                    response.getVersion()
            );
        }
    }
}

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
import com.alextherapeutics.diga.model.generatedxml.billingreport.MessageType;
import com.alextherapeutics.diga.model.generatedxml.billingreport.Report;
import com.alextherapeutics.diga.model.generatedxml.billingreport.ResourceType;
import com.alextherapeutics.diga.model.generatedxml.billingreport.ValidationStepResultType;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.NachrichtentypStp;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.PruefungFreischaltcode;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A XML reader using JAXB.
 * Depends on XML Schemas (.xsd) located in main/resources/*-xsd/
 */
@Slf4j
public class DigaXmlJaxbRequestReader implements DigaXmlRequestReader {
    private JAXBContext codeValidationContext;
    private Unmarshaller codeValidationUnmarshaller;
    private JAXBContext billingReportContext;
    private Unmarshaller billingReportUnmarshaller;

    public DigaXmlJaxbRequestReader() throws JAXBException {
        codeValidationContext = JAXBContext.newInstance(PruefungFreischaltcode.class);
        codeValidationUnmarshaller = codeValidationContext.createUnmarshaller();
        billingReportContext = JAXBContext.newInstance(Report.class);
        billingReportUnmarshaller = billingReportContext.createUnmarshaller();
    }

    @Override
    public DigaInvoiceResponse readBillingReport(InputStream decryptedReport) throws JAXBException, IOException {
        var bytes = decryptedReport.readAllBytes();
        var report = (Report) billingReportUnmarshaller.unmarshal(new ByteArrayInputStream(bytes));
        return DigaInvoiceResponse.builder()
                .hasError(!report.isValid())
                .errors(getInvoiceErrors(report))
                .rawXmlResponseBody(IOUtils.toString(bytes, "UTF-8"))
                .build();

    }
    @Override
    public DigaCodeValidationResponse readCodeValidationResponse(InputStream decryptedResponse) throws JAXBException, IOException {
        var bytes = decryptedResponse.readAllBytes();
        var response = (PruefungFreischaltcode) codeValidationUnmarshaller.unmarshal(new ByteArrayInputStream(bytes));
        validateCodeValidationResponse(response);

        // appendix 4 at https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp
        // seems to differ from the current xsd schema definition. watch for changes here
        return DigaCodeValidationResponse.builder()
                .rawXmlResponseBody(IOUtils.toString(bytes, "UTF-8"))
                .hasError(response.getNachrichtentyp().equals(NachrichtentypStp.FEH))
                .errors(getCodeValidationErrors(response))
                .validatedDigaCode(response.getAntwort() == null ? null : response.getAntwort().getFreischaltcode())
                .dayOfServiceProvision(response.getAntwort() == null ? null : response.getAntwort().getTagDerLeistungserbringung().toGregorianCalendar().getTime())
                .prescriptionType(
                        response.getAntwort() == null
                                ? null
                                : DigaPrescriptionType.fromIdentifier(response.getAntwort().getDiGAVEID().substring(5))
                )
                .build();
    }
    private List<DigaCodeValidationResponseError> getCodeValidationErrors(PruefungFreischaltcode request) {
        var errors = request.getFehlerinformation();
        return errors == null
                ? Collections.emptyList()
                : errors.stream()
                .map(fehlerinformation -> new DigaCodeValidationResponseError(DigaCodeValidationErrorCode.fromCode(fehlerinformation.getFehlernummer().intValue()), fehlerinformation.getFehlertext()))
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
    private List<DigaInvoiceResponseError> getInvoiceErrors(Report report) {
        return report.isValid()
                ? Collections.emptyList()
                : report.getScenarioMatched().getValidationStepResult().stream()
                .filter(Predicate.not(ValidationStepResultType::isValid))
                .map(
                        validationStepResult -> DigaInvoiceResponseError.builder()
                                .validationStepId(validationStepResult.getId())
                                .resources(createResourceInfoFromError(validationStepResult.getResource()))
                                .messages(createMessagesFromError(validationStepResult.getMessage()))
                                .build()
                )
                .collect(Collectors.toList());
    }
    private String createMessagesFromError(List<MessageType> messages) {
        var sb = new StringBuilder();
        sb.append("Messages:\n");
        messages.stream().forEach(
                message -> {
                    sb.append("Message: " + message.getValue());
                    sb.append(", with code: ");
                    sb.append(message.getCode());
                    sb.append(", at xPathLocation: ");
                    sb.append(message.getXpathLocation());
                    sb.append("\n");
                }
        );
        return sb.toString();

    }
    private String createResourceInfoFromError(List<ResourceType> resources) {
        var sb = new StringBuilder();
        sb.append("Resources:\n");
        resources.stream().forEach(
                resource -> {
                    sb.append("Name: " + resource.getName());
                    sb.append(", ");
                    sb.append("Location: " + resource.getLocation());
                    sb.append("\n");
                }
        );
        return sb.toString();
    }

}

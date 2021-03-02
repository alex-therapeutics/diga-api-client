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

import com.alextherapeutics.diga.DigaUtils;
import com.alextherapeutics.diga.DigaXmlRequestWriter;
import com.alextherapeutics.diga.model.DigaCodeInformation;
import com.alextherapeutics.diga.model.DigaSupportedXsdVersion;
import com.alextherapeutics.diga.model.xml.NachrichtentypStp;
import com.alextherapeutics.diga.model.xml.ObjectFactory;
import com.alextherapeutics.diga.model.xml.PruefungFreischaltcode;
import com.alextherapeutics.diga.model.xml.VerfahrenskennungStp;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An XML writer using JAXB
 */
@Slf4j
public class DigaXmlJaxbRequestWriter implements DigaXmlRequestWriter {
    private String senderIk;
    private String digaId;

    private JAXBContext context;
    private Marshaller marshaller;
    private ObjectFactory objectFactory;
    private DatatypeFactory datatypeFactory;

    @Builder
    public DigaXmlJaxbRequestWriter(@NonNull String senderIk, @NonNull String digaId) throws JAXBException {
        this.senderIk = DigaUtils.ikNumberWithoutPrefix(senderIk);
        this.digaId = digaId;
        init();
    }

    private void init() throws JAXBException {
        objectFactory = new ObjectFactory();
        context = JAXBContext.newInstance(PruefungFreischaltcode.class);
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        datatypeFactory = DatatypeFactory.newDefaultInstance();
    }

    @Override
    public byte[] createCodeValidationRequest(DigaCodeInformation codeInformation) throws JAXBException, IOException {
        var processIdentifier = DigaUtils.isDigaTestCode(codeInformation.getFullDigaCode())
                ? VerfahrenskennungStp.TDFC_0
                : VerfahrenskennungStp.EDFC_0;


        var receiverIkWithoutPrefix = DigaUtils.ikNumberWithoutPrefix(codeInformation.getInsuranceCompanyIKNumber());
        var anfrage = objectFactory.createPruefungFreischaltcodeAnfrage();
        anfrage.setIKDiGAHersteller(senderIk);
        anfrage.setIKKrankenkasse(receiverIkWithoutPrefix);
        anfrage.setDiGAID(digaId);
        anfrage.setFreischaltcode(codeInformation.getFullDigaCode());

        var request = objectFactory.createPruefungFreischaltcode();
        request.setAnfrage(anfrage);
        request.setVerfahrenskennung(processIdentifier);
        request.setGueltigab(datatypeFactory.newXMLGregorianCalendar(DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_DATE.getValue()));
        request.setAbsender(senderIk);
        request.setEmpfaenger(receiverIkWithoutPrefix);
        request.setNachrichtentyp(NachrichtentypStp.ANF);
        request.setVersion(DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_VERSION.getValue());

        try (var res = new ByteArrayOutputStream()) {
            marshaller.marshal(request, res);
            return res.toByteArray();
        }
    }
}

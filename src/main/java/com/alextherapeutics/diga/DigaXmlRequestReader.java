package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaApiResponse;
import com.alextherapeutics.diga.model.DigaApiResponseError;
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
 * Reads decrypted XML requests (and responses) and outputs more readable Java objects.
 */
@Slf4j
public class DigaXmlRequestReader {
    private JAXBContext context;
    private Unmarshaller unmarshaller;

    public DigaXmlRequestReader() throws JAXBException {
        context = JAXBContext.newInstance(PruefungFreischaltcode.class);
        unmarshaller = context.createUnmarshaller();
    }

    public DigaApiResponse readCodeValidationResponse(InputStream decryptedResponse) throws JAXBException, IOException {
        var bytes = decryptedResponse.readAllBytes();
        var response = (PruefungFreischaltcode) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));

        return DigaApiResponse.builder()
                .rawXmlBody(IOUtils.toString(bytes, "UTF-8"))
                .hasError(response.getNachrichtentyp().equals(NachrichtentypStp.FEH))
                .errors(getErrors(response))
                .build();
    }
    private List<DigaApiResponseError> getErrors(PruefungFreischaltcode request) {
        var errors = request.getFehlerinformation();
        return errors == null
                ? Collections.emptyList()
                : errors.stream()
                .map(fehlerinformation -> new DigaApiResponseError(fehlerinformation.getFehlernummer().intValue(), fehlerinformation.getFehlertext()))
                .collect(Collectors.toList());
    }
}

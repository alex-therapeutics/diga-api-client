package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaApiResponse;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads decrypted raw XML request/response bodies and outputs more readable Java objects.
 */
public interface DigaXmlRequestReader {
    /**
     * Read an inputstream with XML contents and parse it.
     * @param decryptedResponse
     * @return Relevant information in a new {@link DigaApiResponse} object. Note that the DigaApiResponse object contains fields which are not set by XML readers, like the HTTP status code.
     * @throws JAXBException
     * @throws IOException
     */
    DigaApiResponse readCodeValidationResponse(InputStream decryptedResponse) throws JAXBException, IOException;
}

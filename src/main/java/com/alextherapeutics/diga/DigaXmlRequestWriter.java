package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaCodeInformation;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * Creates raw XML data bodies for code validation requests.
 */
public interface DigaXmlRequestWriter {
    /**
     * Create a XML request body for DiGA code validation (A "PruefungFreischaltCode - Anfrage")
     * @return A byte array containing a (non-encrypted) PruefungFreischaltcode - Anfrage XML request
     * @throws JAXBException
     * @throws IOException
     */
    byte[] createCodeValidationRequest(DigaCodeInformation codeInformation) throws JAXBException, IOException;
}

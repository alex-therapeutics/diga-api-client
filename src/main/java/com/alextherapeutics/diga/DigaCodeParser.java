package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaCodeInformation;

/**
 * Parses DiGA code returning an information object containing information gathered
 * from the insurance company mapping list at https://kkv.gkv-diga.de/
 */
public interface DigaCodeParser {
    /**
     * Parse a full DiGA code which was input into the DiGA by a patient.
     * @param code
     * @return A {@link DigaCodeInformation} object containing information derived from the parsing.
     * @throws DigaCodeValidationException
     */
    DigaCodeInformation parseCode(String code) throws DigaCodeValidationException;
}

package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.xml.KrankenkasseninformationCtp;

/**
 * Contains the information located in the health insurance data mapping file provided by gkv at
 * https://kkv.gkv-diga.de/ and provides methods for accessing the information.
 */
public interface DigaHealthInsuranceDirectory {
    /**
     * Get information from the insurance directory based on the company code prefix,
     * or "Kostentraegerkuerzel"
     * @param prefix
     * @return
     */
    // TODO provide english mapping of this model
    KrankenkasseninformationCtp getInformation(String prefix);
}

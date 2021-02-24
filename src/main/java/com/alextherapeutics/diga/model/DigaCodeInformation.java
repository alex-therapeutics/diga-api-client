package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Contains information gained from a (parsed) DiGA code when compared with the insurance company mapping file
 */
@Builder
@Getter
public class DigaCodeInformation {
    private String insuranceCompanyIKNumber;
    private String insuranceCompanyName;
    private String endpoint;
    /**
     * The full (unparsed) 16 character diga code
     */
    private String fullDigaCode;
    /**
     * The individual 12 character part of the diga code (Krankenkassenindividueller Code)
     */
    private String personalDigaCode;
}

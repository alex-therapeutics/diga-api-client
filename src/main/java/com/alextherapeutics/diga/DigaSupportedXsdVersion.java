package com.alextherapeutics.diga;

import lombok.Getter;

/**
 * Supported xsd version constants
 */
public enum DigaSupportedXsdVersion {

    DIGA_CODE_VALIDATION_VERSION("002.000.000"),
    DIGA_CODE_VALIDATION_DATE("2020-07-01");

    @Getter
    private String value;

    DigaSupportedXsdVersion(String value) {
        this.value = value;
    }
}

package com.alextherapeutics.diga.model;

import lombok.Getter;

/**
 * Supported xsd version constants.
 * These are the versions currently supported by this library.
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

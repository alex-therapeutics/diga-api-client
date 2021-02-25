package com.alextherapeutics.diga;

import lombok.Getter;

public enum DigaSupportedXsdVersion {

    DIGA_CODE_VALIDATION("002.000.000");

    @Getter
    private String versionNumber;

    DigaSupportedXsdVersion(String versionNumber) {
        this.versionNumber = versionNumber;
    }
}

package com.alextherapeutics.diga;

import lombok.Getter;

/**
 * Test codes valid for the DiGA API
 * From Appendix 6 at
 * https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp
 */
public enum DigaApiTestCode {
    VALID("77AAAAAAAAAAAAAX"),
    ERROR_CODE_HAS_TIMED_OUT("77AAAAAAAAAAADEV"),
    ERROR_CODE_CANCELLED("77AAAAAAAAAAADFF"),
    ERROR_CODE_NOT_FOUND("77AAAAAAAAAAADGE"),
    ERROR_REQUEST_OR_DATA_INVALID("77AAAAAAAAAAAGIS"),
    ERROR_SERVER_FAILURE("77AAAAAAAAAAAGJC"),
    ERROR_SERVER_MEMORY_FAILURE("77AAAAAAAAAAAGKD")
    ;

    @Getter
    private String code;
    DigaApiTestCode(String code) {
        this.code = code;
    }
}

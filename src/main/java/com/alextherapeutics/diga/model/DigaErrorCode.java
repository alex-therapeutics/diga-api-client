package com.alextherapeutics.diga.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Error codes corresponding to Annex 5 at
 * https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp
 */
public enum DigaErrorCode {
    CODE_TIMED_OUT(100),
    CODE_CANCELLED(101),
    CODE_NOT_FOUND(102),
    REQUEST_OR_DATA_INVALID(200),
    SERVER_ERROR(201),
    SERVER_MEMORY_ERROR(202);

    @Getter
    private int code;
    DigaErrorCode(int code) {
        this.code = code;
    }

    private static final Map<Integer, DigaErrorCode> BY_CODE = new HashMap<>();
    static {
        for (DigaErrorCode code : values()) {
            BY_CODE.put(code.code, code);
        }
    }
    public static DigaErrorCode fromCode(int code) {
        return BY_CODE.get(code);
    }
}

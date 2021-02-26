package com.alextherapeutics.diga.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * This specification needs more clarity as it has been reported to work this way by developers but not in the
 * official documentation.
 * If your DiGA is the same price no matter if it's a first-time purchase or a renewal, this might just be unspecified
 * each time. Or in the future some new values might show up that we haven't seen before.
 */
@Slf4j
public enum DigaPrescriptionType {
    UNSPECIFIED("000"),
    INITIAL("001"),
    RENEWAL("002");

    @Getter
    private String identifier;
    DigaPrescriptionType(String identifier) {
        this.identifier = identifier;
    }
    private static final Map<String, DigaPrescriptionType> BY_IDENTIFIER = new HashMap<>();
    static {
        for (DigaPrescriptionType type : values()) {
            BY_IDENTIFIER.put(type.identifier, type);
        }
    }

    /**
     * Return the enum value by identifier.
     * If the identifier is unknown, the enum value 'UNSPECIFIED' will be returned and an error be logged.
     * @param identifier
     * @return
     */
    public static DigaPrescriptionType fromIdentifier(String identifier) {
        try {
            return BY_IDENTIFIER.get(identifier);
        } catch (NullPointerException e) {
            log.error("Received a DiGA-VE-ID code with an unexpected value: {}. Please report this to the maintainers. Returning 'UNSPECIFIED'.", identifier);
            return UNSPECIFIED;
        }
    }
}

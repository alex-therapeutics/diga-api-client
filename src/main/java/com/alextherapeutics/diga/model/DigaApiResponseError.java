package com.alextherapeutics.diga.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Error information received from an XML response
 */
@AllArgsConstructor
@Data
public class DigaApiResponseError {
    /**
     * An API error code as described in the documentation at
     */
    private DigaErrorCode errorCode;
    /**
     * A description of the error
     */
    private String errorText;
}

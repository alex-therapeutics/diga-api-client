package com.alextherapeutics.diga;

/**
 * The provided DiGA Code was not valid.
 */
public class DigaCodeValidationException extends DigaApiException {
    public DigaCodeValidationException(String msg) {
        super(msg);
    }
}

package com.alextherapeutics.diga;

/**
 * The DiGA HTTP client failed to perform the specified task
 */
public class DigaHttpClientException extends Exception {
    public DigaHttpClientException(String msg) {
        super(msg);
    }
}

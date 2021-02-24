package com.alextherapeutics.diga;

/**
 * Thrown when an attempt to create a new diga http client fails.
 */
public class DigaHttpClientException extends Exception {
    DigaHttpClientException(String msg) {
        super(msg);
    }
}

package com.alextherapeutics.diga;

/**
 * The DiGA API client failed to initialize
 */
public class DigaApiException extends Exception {
    public DigaApiException(String msg) {
        super(msg);
    }
    public DigaApiException(Exception e) {
        super("Exception thrown: " + e.toString());
    }
    public DigaApiException(Exception e, String msg) {
        super("Exception thrown: " + e.toString() + "\nWith message: " + msg);
    }
}

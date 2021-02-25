package com.alextherapeutics.diga;

/**
 * The DiGA API client failed to initialize
 */
public class DigaApiException extends Exception {
    public DigaApiException(String msg) {
        super(msg);
    }
    public DigaApiException(Throwable e) {
        super("Exception thrown: " + e.toString(), e);
    }
    public DigaApiException(Throwable e, String msg) {
        super("Exception thrown: " + e.toString() + "\nWith message: " + msg, e);
    }
}

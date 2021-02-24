package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * A response from a DiGA API with the original encrypted body.
 */
@Builder
@Getter
public class DigaApiResponseEncrypted {
    @NonNull
    private int statusCode;
    @NonNull
    private String senderIK;
    @NonNull
    private String recipientIK;
    @NonNull
    private String verfahren;
    @NonNull
    private byte[] encryptedBody;
}

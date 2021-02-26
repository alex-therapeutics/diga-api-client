package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * A HTTP request to a DiGA API endpoint.
 */
@Builder
@Getter
public class DigaApiHttpRequest {
    private final String mediaType = "multipart/form-data";
    /**
     * The Process type.
     */
    // TODO make enum and change name
    @Builder.Default
    private String verfahren = "EDFC0";
    /**
     * The full URL (including https etc) of the DiGA API endpoint to send this to.
     */
    @NonNull
    private String url;
    /**
     * The IK of the sender of the request. This will most likely be your IK.
     */
    @NonNull
    private String senderIK;
    /**
     * The IK of the receiver of the request. This will most likely be the IK of the DiGA API endpoint provider.
     */
    @NonNull
    private String recipientIK;
    /**
     * The encrypted XML data request body which should be sent with the request.
     */
    @NonNull
    private byte[] encryptedContent;

}
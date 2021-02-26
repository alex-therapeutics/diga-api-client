package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * A response from a DiGA API with the original encrypted body.
 */
@Builder
@Getter
public class DigaApiHttpResponse {
    /**
     * The HTTP status code received
     */
    @NonNull
    private int statusCode;
    /**
     * The IK of the sender of this response. Note that this will usually correspond to the sender of the API endpoint
     * provider in this case (the Response), whereas in the case of the {@link DigaApiHttpRequest}, the senderIk will likely be you.
     */
    @NonNull
    private String senderIK;
    /**
     * The IK of the receiver of this response. This will likely be your IK.
     */
    @NonNull
    private String recipientIK;
    /**
     * Process ID.
     */
    // TODO make Enum and change name
    @NonNull
    private String verfahren;
    /**
     * The body contained in the request. This will consist of XML data encrypted with your certificate, which
     * needs to be decrypted using your private key.
     */
    @NonNull
    private byte[] encryptedBody;
}

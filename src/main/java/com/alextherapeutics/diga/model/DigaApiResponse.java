package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Collects the information received from an API response
 * Mutable object interacted with by several interfaces: Contains information from both the HTTP response as well as the XML response, and the original request.
 */
@Builder
@Data
public class DigaApiResponse {
    /**
     * The code which has been validated.
     */
    private String validatedDigaCode;

    /**
     * Which type of prescription was granted (renewal, initial)
     */
    private DigaPrescriptionType prescriptionType;

    // TODO figure out what this translation means ("tag der leistungserbringung" from "antwort")
    private Date dayOfServiceProvision;
    /**
     * The HTTP code received from the API. Note that this does not inform us on if our XML request was successful,
     * only if the request was successfully processed. For example, you can get HTTP 200 but the XML body can contain
     * an error code explaining that your XML request was unsuccessful. You probably don't have to use this field.
     */
    private int httpStatusCode;
    /**
     * Whether the response says your request had errors.
     */
    @Builder.Default
    private boolean hasError = false;
    /**
     * Information on the errors.
     */
    private List<DigaApiResponseError> errors;
    /**
     * The raw decrypted XML body encoded as an UTF-8 string.
     */
    private String rawXmlResponseBody;
    /**
     * The raw XML body pre-encryption encoded as an UTF-8 string.
     */
    private String rawXmlRequestBody;
    /**
     * The raw **encrypted** XML body.
     */
    private byte[] rawXmlRequestBodyEncrypted;
}

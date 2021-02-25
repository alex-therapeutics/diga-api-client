package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Collects the information received from an API response
 * Contains information from both the HTTP response as well as the XML response
 */
@Builder
@Data
public class DigaApiResponse {
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
     * The raw decrypted XML body encoded as an UTF-8 string, in case you wish to use it for something.
     */
    private String rawXmlBody;
}

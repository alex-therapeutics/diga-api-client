package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaApiHttpRequest;
import com.alextherapeutics.diga.model.DigaApiHttpResponse;

/**
 * A HTTP client that handles HTTP(S) communication with DiGA API endpoints.
 */
public interface DigaHttpClient {
    /**
     * POST a DigaApiRequest
     * @param request
     * @return A {@link DigaApiHttpResponse} containing response information including the encrpyted data body (if received)
     * @throws DigaHttpClientException
     */
    DigaApiHttpResponse post(DigaApiHttpRequest request) throws DigaHttpClientException;
}

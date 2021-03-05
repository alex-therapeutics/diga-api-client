/*
 * Copyright 2021-2021 Alex Therapeutics AB and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Common fields for all responses from the DiGA API
 */
@SuperBuilder
@Data
public abstract class AbstractDigaApiResponse {
    /**
     * The HTTP code received from the API. Note that this does not inform us on if our XML request was successful,
     * only if the request was successfully processed. For example, you can get HTTP 200 but the XML body can contain
     * an error code explaining that your XML request was invalid. You probably only have to use this field for debugging.
     */
    private int httpStatusCode;
    /**
     * Whether the XML response says your XML request had errors.
     */
    @Builder.Default
    private boolean hasError = false;
    /**
     * The raw decrypted XML response body encoded as an UTF-8 string.
     */
    private String rawXmlResponseBody;
    /**
     * The raw XML request body pre-encryption encoded as an UTF-8 string.
     */
    private String rawXmlRequestBody;
    /**
     * The raw **encrypted** XML body.
     */
    private byte[] rawXmlRequestBodyEncrypted;
    /**
     * The name of the company that the request was sent to
     */
    private String receivingInsuranceCompanyName;
    /**
     * The IK of the company that the request was sent to
     */
    private String receivingInsuranceCompanyIk;
    /**
     * The endpoint that the request was sent to
     */
    private String receivingInsuranceCompanyEndpoint;
}

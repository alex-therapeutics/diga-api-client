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

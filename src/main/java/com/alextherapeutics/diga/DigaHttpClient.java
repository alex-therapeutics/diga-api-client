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

package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaApiHttpRequest;
import com.alextherapeutics.diga.model.DigaApiHttpResponse;

/** A HTTP client that handles HTTP(S) communication with DiGA API endpoints. */
public interface DigaHttpClient {
  /**
   * POST a DigaApiRequest
   *
   * @param request
   * @return A {@link DigaApiHttpResponse} containing response information including the encrpyted
   *     data body (if received)
   * @throws DigaHttpClientException if the request failed
   */
  DigaApiHttpResponse post(DigaApiHttpRequest request) throws DigaHttpClientException;
}

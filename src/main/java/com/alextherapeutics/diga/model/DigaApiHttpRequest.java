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

/** A HTTP request to a DiGA API endpoint. */
@Builder
@Getter
public class DigaApiHttpRequest {
  private final String mediaType = "multipart/form-data";

  /** The Process type. */
  @NonNull private final DigaProcessCode processCode;

  /** The full URL (including https etc) of the DiGA API endpoint to send this to. */
  @NonNull private final String url;

  /** The IK of the sender of the request. This will most likely be your IK. */
  @NonNull private final String senderIK;

  /**
   * The IK of the receiver of the request. This will most likely be the IK of the DiGA API endpoint
   * provider.
   */
  @NonNull private final String recipientIK;

  /** The encrypted XML data request body which should be sent with the request. */
  @NonNull private final byte[] encryptedContent;
}

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

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/** A DiGA correction invoice request */
@SuperBuilder
@Getter
public class DigaCorrectionInvoice extends DigaInvoice {
  /**
   * The ID of the reference invoice. You set this to the original invoice id to reference it in
   * your billing system.
   */
  private final String referenceInvoiceId;

  @NonNull private final String correctionCode;

  private final String[] correctionCodeReason;
}

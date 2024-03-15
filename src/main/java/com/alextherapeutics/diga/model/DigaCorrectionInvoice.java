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

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/** A DiGA correction invoice request */
@Builder
@Getter
public class DigaCorrectionInvoice {
  /**
   * The ID of the corrected invoice. You set this to something unique which fits with your billing
   * system.
   */
  @NonNull private final String invoiceId;

  /**
   * The ID of the reference invoice. You set this to the original invoice id to reference it in
   * your billing system.
   */
  private final String referenceInvoiceId;

  /** The invoice issue date. Defaults to now() */
  @Builder.Default private final Date issueDate = new Date();

  /** The DiGA code you are charging for. */
  @NonNull private final String validatedDigaCode;

  /** Date of service provision "Tag der Leistungserbringung" */
  @NonNull private final Date dateOfServiceProvision;

  /**
   * The DiGAVEid that was validated by this code. You will find this in the response to the code
   * validation request at {@link DigaCodeValidationResponse#getValidatedDigaveid()}
   */
  @NonNull private final String digavEid;

  @Builder.Default private final String invoiceCurrencyCode = "EUR";

  @NonNull private final String correctionCode;

  private final String[] correctionCodeReason;
}

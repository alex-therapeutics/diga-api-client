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
import lombok.experimental.SuperBuilder;

/** Information for billing derived from parsing a DiGA code. */
@SuperBuilder
@Getter
public class DigaBillingInformation extends AbstractDigaInsuranceInformation {
  /**
   * This string is input to fields which are required by the invoice validators but do not need to
   * contain valid information for a DiGA invoice when they are null in the mapping file. see
   * https://github.com/alex-therapeutics/diga-api-client/issues/12#issuecomment-796721204
   */
  public static final String INFORMATION_MISSING = "Information Missing";

  /**
   * The IK for billing, to the creditor of the company being billed. This is sometimes not the same
   * as the company IK.
   */
  @NonNull private final String buyerCompanyCreditorIk;

  /** How the company accepts invoices */
  @NonNull private final DigaInvoiceMethod buyerInvoicingMethod;

  /** The postal code of the company being billed. */
  private final String buyerCompanyPostalCode;

  /** The address line of the company being billed. F.e "Teststreet 1" */
  private final String buyerCompanyAddressLine;

  /** The name of the company being billed. */
  private final String buyerCompanyCity;

  /** The country code of the company being billed. */
  @Builder.Default private final String buyerCompanyCountryCode = "DE";

  /**
   * The company's invoice email This can be null as it only affects companies which have {@link
   * DigaInvoiceMethod#EMAIL}
   */
  private final String buyerInvoicingEmail;
}

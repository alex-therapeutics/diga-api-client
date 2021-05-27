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

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Information about the DiGA and the manufacturer which doesnt change between requests. Used for
 * sending the correct information when validating codes and creating invoices.
 */
@Builder
@Getter
public class DigaInformation {
  /**
   * The DiGA ID of the DiGA which is using this client. If you are serving more than one DiGA from
   * this backend, you will need two instances of the client.
   */
  @NonNull private final String digaId;
  /**
   * The common name of the DiGA which is using this client. If you are serving more than one DiGA
   * from this backend, you will need two instances of the client. This is needed for billing.
   */
  @NonNull private final String digaName;
  /**
   * A description of the item being sold. This defaults to "A (your-diga-name) prescription." Set
   * this field if you wish to change the default.
   */
  private final String digaDescription;
  /** The name of the manufacturing company. This is probably your company's name. */
  @NonNull private final String manufacturingCompanyName;
  /** The IK number of your company sending requests from this API client. */
  @NonNull private final String manufacturingCompanyIk;
  /**
   * The net price of one prescription. This should be the net price to be invoiced when you
   * validate one DiGA code.
   */
  @NonNull private final BigDecimal netPricePerPrescription;
  /**
   * The VAT percent applicable to a sale as a number between 0 and 100 (if it is 50% then put 50
   * here, not 0.5). Defaults to 19%. If {@link #reverseChargeVAT} is true, then this value doesn't
   * matter as the applicable percentage will be set to 0 in the invoice.
   */
  @Builder.Default private final BigDecimal applicableVATpercent = new BigDecimal(19);

  /**
   * If the VAT should be reverse charge (for example for international companies operating outside
   * of Germany). This sets the VAT type on invoices to reverse charge and adjusts other related
   * values. Defaults to 'false' which uses Standard VAT type.
   */
  @Builder.Default private final boolean reverseChargeVAT = false;

  /** The VAT registration code for your company. Something like "DE 123 456 789" */
  @NonNull private final String manufacturingCompanyVATRegistration;

  /**
   * Contact details for the person who is the DiGA seller contact person on invoices
   * ("DefinedTradeContact")
   */
  @NonNull private final ContactPersonForBilling contactPersonForBilling;

  /** Company address details for invoices */
  @NonNull private final CompanyTradeAddress companyTradeAddress;

  @Builder
  @Getter
  public static class CompanyTradeAddress {
    /** Postal code */
    @NonNull private final String postalCode;
    /** The address line for the postal address of the seller. F.e "Musterstraße 1" */
    @NonNull private final String adressLine;
    /** City */
    @NonNull private final String city;
    /** Country code, f.e "DE" */
    @NonNull private final String countryCode;
  }

  @Builder
  @Getter
  public static class ContactPersonForBilling {
    /** The full name, f.e "Testy Testsson" */
    @NonNull private final String fullName;
    /** Phone number to the contact person */
    @NonNull // can be null?
    private final String phoneNumber;
    /** Email address */
    @NonNull // maybe this can be null?
    private final String emailAddress;
  }
}

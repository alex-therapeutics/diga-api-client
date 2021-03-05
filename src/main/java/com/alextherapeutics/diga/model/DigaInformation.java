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

import java.math.BigDecimal;

/**
 * Information about the DiGA and the manufacturer which doesnt change between requests.
 * Used for sending the correct information when validating codes and creating invoices.
 */
@Builder
@Getter
public class DigaInformation {
    /**
     * The DiGA ID of the DiGA which is using this client. If you are serving more than one DiGA from
     * this backend, you will need two instances of the client.
     */
    @NonNull
    private String digaId;
    /**
     * The common name of the DiGA which is using this client. If you are serving more than one DiGA from
     * this backend, you will need two instances of the client. This is needed for billing.
     */
    @NonNull
    private String digaName;
    /**
     * A description of the item being sold. This defaults to "A (your-diga-name) prescription."
     * Set this field if you wish to change the default.
     */
    private String digaDescription;

    /**
     * The ID of the manufacturing company.
     */
    // TODO - what is difference between this and name?
    @NonNull
    private String manufacturingCompanyId;
    /**
     * The name of the manufacturing company. This is probably your company's name.
     */
    @NonNull
    private String manufacturingCompanyName;
    /**
     * The IK number of your company sending requests from this API client.
     */
    @NonNull
    private String manufacturingCompanyIk;
    /**
     * The net price of one prescription.
     * This should be the net price to be invoiced when you validate one DiGA code.
     */
    @NonNull
    private BigDecimal netPricePerPrescription;
    /**
     * The VAT percent applicable to a sale as a number between 0 and 100 (if it is 50% then put 50 here, not 0.5).
     */
    @NonNull
    private BigDecimal applicableVATpercent;
    /**
     * The VAT registration code for your company. Something like "DE 123 456 789"
     */
    @NonNull
    private String manufacturingCompanyVATRegistration;

    /**
     * Contact details for the person who is the DiGA seller contact person on invoices ("DefinedTradeContact")
     */
    @NonNull
    private ContactPersonForBilling contactPersonForBilling;

    /**
     * Company address details for invoices
     */
    @NonNull
    private CompanyTradeAddress companyTradeAddress;

    @Builder
    @Getter
    public static class CompanyTradeAddress {
        /**
         * Postal code
         */
        @NonNull
        private String postalCode;
        /**
         * The address line for the postal address of the seller. F.e "Musterstraße 1"
         */
        @NonNull
        private String adressLine;
        /**
         * City
         */
        @NonNull
        private String city;
        /**
         * Country code, f.e "DE"
         */
        @NonNull
        private String countryCode;
    }
    @Builder
    @Getter
    public static class ContactPersonForBilling {
        /**
         * The full name, f.e "Testy Testsson"
         */
        @NonNull
        private String fullName;
        /**
         * Phone number to the contact person
         */
        @NonNull // can be null?
        private String phoneNumber;
        /**
         * Email address
         */
        @NonNull // maybe this can be null?
        private String emailAddress;
    }
}

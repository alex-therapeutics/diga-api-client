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
import java.util.Date;

/**
 * A DiGA invoice request
 */
@Builder
@Getter
public class DigaInvoice {
    /**
     * The ID of the invoice.
     * You set this to something unique which fits with your billing system.
     */
    @NonNull
    private String invoiceId;
    /**
     * The invoice issue date. Defaults to now()
     */
    @Builder.Default
    private Date issueDate = new Date();

    /**
     * The DiGA code you are charging for.
     */
    @NonNull
    private String validatedDigaCode;

    /**
     * Date of service provision "Tag der Leistungserbringung" (this is in the response)
     */
    @Builder.Default
    private Date dateOfServiceProvision = new Date();

    /**
     * The prescription type. This is in practice 3 digits appended to your diga ID
     * to form a "digaveid". We still don't know quite what this means, see {@link DigaPrescriptionType}
     * We start by always appending 000. Might want to change to 001 if 000 doesnt work.
     */
    @Builder.Default
    private DigaPrescriptionType prescriptionType = DigaPrescriptionType.UNSPECIFIED;

    /**
     * A description of the item being sold. This defaults to "A (your-diga-name) prescription."
     * Set this field if you wish to change the default.
     */
    private String digaDescription;

    // TODO mb move some "static" values for each diga to
    // the diga api client builder, so you can input stuff like
    // diga description, net price, VAT, etc. from the start and not
    // in every billing request
    // they are gathered below
    /**
     * The net price of your sale.
     */
    @NonNull
    private BigDecimal netPrice;

    /**
     * The VAT percent applicable to this sale as a number between 0 and 100 (if it is 50% then put 50 here, not 0.5).
     */
    @NonNull
    private BigDecimal applicableVATpercent;

    // TODO ?? what is difference between this and name
    @NonNull
    private String sellerCompanyId;
    /**
     * The name of the selling company. This is probably your company's name.
     */
    @NonNull
    private String sellerCompanyName;

    /**
     * The VAT registration code for your company. Something like "DE 123 456 789"
     */
    @NonNull
    private String sellerCompanyVATRegistration;

    // TODO refactor this to something better, own class f.e or use the trade party class
    /**
     * The full name of the seller's contact person.
     */
    @NonNull
    private String sellerContactPersonFullName;
    @NonNull
    private String sellerContactPersonPhoneNumber;
    @NonNull
    private String sellerContactPersonEmailAddress;
    @NonNull
    private String sellerPostalCode;
    /**
     * The address line for the postal address of the seller. F.e "Musterstraße 1"
     */
    @NonNull
    private String sellerAdressLine;
    @NonNull
    private String sellerCity;
    @NonNull
    private String sellerCountryCode;

    // TODO - let price etc be optional value here, or you can set defaults on creating the api client

    @Builder.Default
    private String invoiceCurrencyCode = "EUR";
}

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

    @Builder.Default
    private String invoiceCurrencyCode = "EUR";
}

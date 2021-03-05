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

/**
 * Information for billing derived from parsing a DiGA code.
 */
@SuperBuilder
@Getter
public class DigaBillingInformation extends AbstractDigaInsuranceInformation {
    /**
     * The ID of the company being billed.
     */
    // TODO what is difference with companyname?
    @NonNull
    private String buyerCompanyId;
    /**
     * The IK for billing, to the creditor of the company being billed. This is sometimes not the same as the company IK.
     */
    @NonNull
    private String buyerCompanyCreditorIk;
    /**
     * The postal code of the company being billed.
     */
    private String buyerCompanyPostalCode;
    /**
     * The address line of the company being billed. F.e "Teststreet 1"
     */
    private String buyerCompanyAddressLine;
    /**
     * The name of the company being billed.
     */
    private String buyerCompanyCity;
    /**
     * The country code of the company being billed.
     */
    @Builder.Default
    private String buyerCompanyCountryCode = "DE";
}

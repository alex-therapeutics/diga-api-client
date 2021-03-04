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

/**
 * Information for billing derived from parsing a DiGA code.
 */
@Builder
@Getter
public class DigaBillingInformation {
    // TODO doc
    @NonNull
    private String endpoint;
    @NonNull
    private String buyerCompanyId;
    @NonNull
    private String buyerCompanyIk;
    @NonNull
    private String buyerCompanyName;
    private String buyerCompanyPostalCode;
    private String buyerCompanyAddressLine;
    private String buyerCompanyCity;
    @Builder.Default
    private String buyerCompanyCountryCode = "DE";
    /**
     * The IK for billing (creditor). This is sometimes not the same as the company IK.
     */
    @NonNull
    private String buyerCompanyCreditorIk;
}

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

package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaBillingInformation;
import com.alextherapeutics.diga.model.DigaCodeInformation;

/**
 * Parses DiGA code returning an information object containing information gathered
 * from the insurance company mapping list at https://kkv.gkv-diga.de/
 */
public interface DigaCodeParser {
    /**
     * Parse a full DiGA code which was input into the DiGA by a patient.
     * @param code
     * @return A {@link DigaCodeInformation} object containing information derived from the parsing.
     * @throws DigaCodeValidationException
     */
    DigaCodeInformation parseCodeForValidation(String code) throws DigaCodeValidationException;

    /**
     * Parse a full DiGA code for billing information.
     * @param code
     * @return Billing information for the company that generated the code.
     * @throws DigaCodeValidationException
     */
    DigaBillingInformation parseCodeForBilling(String code) throws DigaCodeValidationException;
}

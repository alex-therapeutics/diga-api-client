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

/**
 * Contains information gained from a (parsed) DiGA code when compared with the insurance company mapping file
 */
@Builder
@Getter
public class DigaCodeInformation {
    /**
     * The IK number of the insurance company that generated this code.
     */
    private String insuranceCompanyIKNumber;
    /**
     * The name of the insurance company that generated this code.
     */
    private String insuranceCompanyName;
    /**
     * The API endpoint of the insurance company that generated this code.
     */
    private String endpoint;
    /**
     * The full (unparsed) 16 character diga code.
     */
    private String fullDigaCode;
    /**
     * The individual 12 character part of the diga code (Krankenkassenindividueller Code)
     */
    private String personalDigaCode;
}

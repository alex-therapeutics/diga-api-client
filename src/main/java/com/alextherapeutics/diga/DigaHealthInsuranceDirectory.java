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

import com.alextherapeutics.diga.model.generatedxml.codevalidation.KrankenkasseninformationCtp;

/**
 * Contains the information located in the health insurance data mapping file provided by gkv at
 * https://kkv.gkv-diga.de/ and provides methods for accessing the information.
 */
public interface DigaHealthInsuranceDirectory {
    /**
     * Get information from the insurance directory based on the company code prefix,
     * or "Kostentraegerkuerzel"
     *
     * @param prefix
     * @return
     */
    // TODO provide english mapping of this model
    KrankenkasseninformationCtp getInformation(String prefix);
}

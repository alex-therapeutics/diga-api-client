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

import lombok.Getter;

/**
 * Test codes valid for the DiGA API
 * From Appendix 6 at
 * https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp
 */
public enum DigaApiTestCode {
    VALID("77AAAAAAAAAAAAAX"),
    ERROR_CODE_HAS_TIMED_OUT("77AAAAAAAAAAADEV"),
    ERROR_CODE_CANCELLED("77AAAAAAAAAAADFF"),
    ERROR_CODE_NOT_FOUND("77AAAAAAAAAAADGE"),
    ERROR_REQUEST_OR_DATA_INVALID("77AAAAAAAAAAAGIS"),
    ERROR_SERVER_FAILURE("77AAAAAAAAAAAGJC"),
    ERROR_SERVER_MEMORY_FAILURE("77AAAAAAAAAAAGKD")
    ;

    @Getter
    private String code;
    DigaApiTestCode(String code) {
        this.code = code;
    }
}

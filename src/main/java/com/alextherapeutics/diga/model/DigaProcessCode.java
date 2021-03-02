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

import java.util.HashMap;
import java.util.Map;

/**
 * Valid DiGA API process codes.
 * These go in the "Verfahrenskennung" header so that the API endpoint understands which type of request it is
 */
public enum DigaProcessCode {
    CODE_VALIDATION("EDFC0"),
    CODE_VALIDATION_TEST("TDFC0"),
    BILLING("EDRE0"),
    BILLING_TEST("TDRE0");

    @Getter
    private String code;
    DigaProcessCode(String code) {
        this.code = code;
    }
    private static final Map<String, DigaProcessCode> BY_CODE = new HashMap<>();
    static {
        for (DigaProcessCode code : values()) {
            BY_CODE.put(code.code, code);
        }
    }
    public static DigaProcessCode fromCode(int code) {
        return BY_CODE.get(code);
    }
}

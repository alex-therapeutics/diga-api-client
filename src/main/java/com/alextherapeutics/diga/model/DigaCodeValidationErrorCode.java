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
 * Code Validation error codes listed in Annex 5 at
 * https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp
 */
public enum DigaCodeValidationErrorCode {
    CODE_TIMED_OUT(100),
    CODE_CANCELLED(101),
    CODE_NOT_FOUND(102),
    REQUEST_OR_DATA_INVALID(200),
    SERVER_ERROR(201),
    SERVER_MEMORY_ERROR(202);

    private static final Map<Integer, DigaCodeValidationErrorCode> BY_CODE = new HashMap<>();

    static {
        for (DigaCodeValidationErrorCode code : values()) {
            BY_CODE.put(code.code, code);
        }
    }

    @Getter
    private int code;

    DigaCodeValidationErrorCode(int code) {
        this.code = code;
    }

    public static DigaCodeValidationErrorCode fromCode(int code) {
        return BY_CODE.get(code);
    }
}

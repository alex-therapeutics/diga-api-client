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

/** Supported xsd version constants. These are the versions currently supported by this library. */
public enum DigaSupportedXsdVersion {
  DIGA_CODE_VALIDATION_VERSION("003.000.000"),
  DIGA_CODE_VALIDATION_DATE("2022-08-25");

  @Getter private final String value;

  DigaSupportedXsdVersion(String value) {
    this.value = value;
  }
}

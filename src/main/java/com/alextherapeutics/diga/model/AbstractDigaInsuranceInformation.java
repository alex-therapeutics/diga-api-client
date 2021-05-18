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
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/** Information about an insurance company gathered from parsing a DiGA code. */
@SuperBuilder
@Getter
public abstract class AbstractDigaInsuranceInformation {
  /** The IK number of the insurance company that generated this code. */
  @NonNull private final String insuranceCompanyIKNumber;
  /** The clearing centers IK number of the insurance that generated this code. */
  @NonNull private final String clearingCenterIKNumber;
  /** The name of the insurance company that generated this code. */
  @NonNull private final String insuranceCompanyName;
  /** The API endpoint of the insurance company that generated this code. */
  @NonNull private final String endpoint;
}

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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DigaApiResponseErrorTest {

  @Test
  void asCodeValidationError() {
    DigaApiResponseError error =
        new DigaCodeValidationResponseError(
            DigaCodeValidationErrorCode.CODE_NOT_FOUND, "Code not found");
    DigaApiResponseError nullError = new DigaApiExceptionError(new Exception());
    assertNull(nullError.asCodeValidationError());
    assertTrue(error.asCodeValidationError() instanceof DigaCodeValidationResponseError);
  }

  @Test
  void asInvoiceResponseError() {
    DigaApiResponseError error = DigaInvoiceResponseError.builder().messages("error").build();
    DigaApiResponseError nullError = new DigaApiExceptionError(new Exception());
    assertNull(nullError.asCodeValidationError());
    assertTrue(error.asInvoiceResponseError() instanceof DigaInvoiceResponseError);
  }

  @Test
  void asApiExceptionError() {
    DigaApiResponseError error = new DigaApiExceptionError(new Exception());
    DigaApiResponseError nullError = DigaInvoiceResponseError.builder().build();
    assertNull(nullError.asApiExceptionError());
    assertTrue(error.asApiExceptionError() instanceof DigaApiExceptionError);
  }
}

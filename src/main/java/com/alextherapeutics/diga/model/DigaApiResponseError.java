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

/**
 * An error returned while communicating with the DiGA API This can both be an error returned from
 * the API endpoints, or an error which occured within the library while interacting with the API
 * endpoint - such as exceptions when decrypting data or sending the http request etc.
 */
public interface DigaApiResponseError {
  /**
   * Get information on this error as a String. This method should be implemented so as to not lose
   * any information contained in the error object itself, so the returned string may be extensive.
   *
   * @return - the error as a string
   */
  String toString();

  /**
   * Synonymous with {@link #toString()}
   *
   * @return
   */
  default String getError() {
    return toString();
  }

  /**
   * Return this error cast as a {@link DigaCodeValidationErrorCode} object.
   *
   * @return - this error casted, or null if this error is not a code validation error.
   */
  default DigaCodeValidationResponseError asCodeValidationError() {
    try {
      return (DigaCodeValidationResponseError) this;
    } catch (ClassCastException e) {
      return null;
    }
  }

  /**
   * Return this error cast as a {@link DigaInvoiceResponseError} object.
   *
   * @return - this error casted, or null if this error is not a invoice response error.
   */
  default DigaInvoiceResponseError asInvoiceResponseError() {
    try {
      return (DigaInvoiceResponseError) this;
    } catch (ClassCastException e) {
      return null;
    }
  }

  /**
   * Return this error cast as a {@link DigaApiExceptionError} object.
   *
   * @return - this error casted, or null if this error is not a diga api exception error.
   */
  default DigaApiExceptionError asApiExceptionError() {
    try {
      return (DigaApiExceptionError) this;
    } catch (ClassCastException e) {
      return null;
    }
  }
}

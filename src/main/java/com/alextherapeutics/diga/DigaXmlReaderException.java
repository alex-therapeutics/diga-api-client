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

/**
 * Thrown when the library fails to read the XML response from the 'nutzdaten' data part of a DiGA
 * API response Inspect the underlying exception for details
 */
public class DigaXmlReaderException extends Exception {
  public DigaXmlReaderException(Throwable e) {
    super("Exception thrown when trying to read XML request body: ", e);
  }
}

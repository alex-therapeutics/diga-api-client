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

import com.alextherapeutics.diga.model.DigaDecryption;
import com.alextherapeutics.diga.model.DigaEncryption;

/** A factory for creating new encryption and decryption attempts. */
public interface DigaEncryptionFactory {
  /**
   * Start a new encryption attempt with the factory defaults.
   *
   * @return A {@link DigaEncryption.DigaEncryptionBuilder} with some defaults configured.
   */
  DigaEncryption.DigaEncryptionBuilder newEncryption();

  /**
   * Start a new decryption attempt with the factory defaults.
   *
   * @return A {@link DigaDecryption.DigaDecryptionBuilder} with some defaults configured.
   */
  DigaDecryption.DigaDecryptionBuilder newDecryption();
}

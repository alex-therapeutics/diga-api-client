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

import com.alextherapeutics.diga.DigaEncryptionException;
import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.SeconException;
import de.tk.opensource.secon.Subscriber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** Attempt to encrypt an inputstream using SECON */
@Builder
@Slf4j
public class DigaEncryption {
  /** The encrypting subscriber */
  @NonNull private final Subscriber subscriber;

  /** The input to encrypt */
  @NonNull private final byte[] encryptionTarget;

  /** The alias of the key in the public key directory */
  @NonNull private final String recipientAlias;

  /**
   * Encrypt the contents as a byte array output stream
   *
   * @return
   */
  public ByteArrayOutputStream encrypt() throws DigaEncryptionException {
    try {
      try (var input = new ByteArrayInputStream(encryptionTarget)) {
        try (var output = new ByteArrayOutputStream()) {
          SECON.copy(
              () -> new ByteArrayInputStream(input.readAllBytes()),
              subscriber.signAndEncryptTo(() -> output, recipientAlias));
          return output;
        }
      }
    } catch (IOException | SeconException e) {
      throw new DigaEncryptionException(e);
    }
  }
}

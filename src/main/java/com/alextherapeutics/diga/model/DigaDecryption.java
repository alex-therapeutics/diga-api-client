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

import com.alextherapeutics.diga.DigaDecryptionException;
import de.tk.opensource.secon.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** Attempt to decrypt a SECON encrypted inputstream */
@Builder
@Slf4j
public class DigaDecryption {
  /** The decrypting subscriber */
  @NonNull private final Subscriber subscriber;
  /** The body to decrypt */
  @NonNull private final byte[] decryptionTarget;

  /**
   * Decrypt the content
   *
   * @return
   * @throws IOException
   * @throws SeconException
   */
  public ByteArrayOutputStream decrypt() throws DigaDecryptionException {
    try {
      try (var outputStream = new ByteArrayOutputStream()) {
        try (var inputStream = new ByteArrayInputStream(decryptionTarget)) {
          SECON.copy(
              subscriber.decryptAndVerifyFrom(() -> inputStream, Verifier.NULL),
              () -> outputStream);
          return outputStream;
        } catch (CertificateNotFoundException e) {
          // there seems to be something wrong in the key list from itsg which means some
          // certificates have the wrong serial number on returning a response. in the future
          // hopefully this is not a necessary catch
          log.debug(
              "Failed to validate sender certificate after (successfully) finishing decrypting the content.\n Since we wouldnt get this far without the server and us having the correct private and public keys for eachother, we pass the decrypted content as a success.");
          return outputStream;
        }
      }
    } catch (IOException | SeconException e) {
      throw new DigaDecryptionException(e);
    }
  }
}

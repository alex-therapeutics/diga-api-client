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

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An invoice method specified for an insurance company For a transitional period, insurance
 * companies are allowed to not accept invoices through the API but via email or post.
 *
 * <p>In these cases, the DiGA manufacturer have to send the invoices manually. For email, you are
 * supposed to send the invoice in XRechnung format to the email specified in the mapping file. For
 * post, it is unclear which format, but you are supposed to send the invoice via the post details
 * in the mapping file.
 */
@AllArgsConstructor
public enum DigaInvoiceMethod {
  /** Invoices should be sent via the API */
  API(1),
  /** Invoices should be sent by email */
  EMAIL(2),
  /** Invoices should be sent by post */
  POST(3);

  private static final Map<Integer, DigaInvoiceMethod> BY_IDENTIFIER = new HashMap<>();

  static {
    for (DigaInvoiceMethod invoiceMethod : values()) {
      BY_IDENTIFIER.put(invoiceMethod.identifier, invoiceMethod);
    }
  }

  @Getter private final int identifier;

  public static DigaInvoiceMethod fromIdentifier(int identifier) {
    return BY_IDENTIFIER.get(identifier);
  }
}

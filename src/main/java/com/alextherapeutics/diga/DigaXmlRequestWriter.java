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

import com.alextherapeutics.diga.model.DigaBillingInformation;
import com.alextherapeutics.diga.model.DigaCodeInformation;
import com.alextherapeutics.diga.model.DigaCorrectionInvoice;
import com.alextherapeutics.diga.model.DigaInvoice;

/** Creates raw XML data bodies containing code validation requests or DiGA invoices. */
public interface DigaXmlRequestWriter {
  /**
   * Create a XML request body for DiGA code validation (A "PruefungFreischaltCode - Anfrage")
   *
   * @param codeInformation - information required to create the request
   * @return A byte array containing a (non-encrypted) PruefungFreischaltcode - Anfrage XML request
   * @throws DigaXmlWriterException - if the request body couldn't be created
   */
  byte[] createCodeValidationRequest(DigaCodeInformation codeInformation)
      throws DigaXmlWriterException;

  /**
   * Create a XML request body containing a DiGA invoice (an "XRechnung" invoice conforming to
   * UN/CEFACT standard)
   *
   * @param invoice - information required to create the invoice
   * @param billingInformation - information on the buyer
   * @return A byte array containing a (non-encrypted) XRechnung XML invoice
   * @throws DigaXmlWriterException - if the request body couldn't be created
   */
  byte[] createBillingRequest(DigaInvoice invoice, DigaBillingInformation billingInformation)
      throws DigaXmlWriterException;

  /**
   * Create a XML request body containing a DiGA correction invoice (an "XRechnung" invoice
   * conforming to UN/CEFACT standard)
   *
   * @param correctionInvoice - information required to create the invoice
   * @param billingInformation - information on the buyer
   * @return A byte array containing a (non-encrypted) XRechnung XML invoice
   * @throws DigaXmlWriterException - if the request body couldn't be created
   */
  byte[] createInvoiceCorrectionRequest(
      DigaCorrectionInvoice correctionInvoice, DigaBillingInformation billingInformation)
      throws DigaXmlWriterException;
}

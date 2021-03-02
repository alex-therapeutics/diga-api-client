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

import com.alextherapeutics.diga.model.DigaCodeInformation;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * Creates raw XML data bodies for code validation requests.
 */
public interface DigaXmlRequestWriter {
    /**
     * Create a XML request body for DiGA code validation (A "PruefungFreischaltCode - Anfrage")
     * @return A byte array containing a (non-encrypted) PruefungFreischaltcode - Anfrage XML request
     * @throws JAXBException
     * @throws IOException
     */
    byte[] createCodeValidationRequest(DigaCodeInformation codeInformation) throws JAXBException, IOException;
}

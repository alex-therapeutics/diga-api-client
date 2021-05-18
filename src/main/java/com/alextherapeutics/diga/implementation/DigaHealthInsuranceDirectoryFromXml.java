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

package com.alextherapeutics.diga.implementation;

import com.alextherapeutics.diga.DigaHealthInsuranceDirectory;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.KostentraegerMappingverzeichnis;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.KrankenkasseninformationCtp;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Unmarshals the contents of the health insurance data mapping file provided by gkv at
 * https://kkv.gkv-diga.de/ and provides methods for accessing the information.
 */
public class DigaHealthInsuranceDirectoryFromXml implements DigaHealthInsuranceDirectory {
  private final JAXBContext context;
  private final Unmarshaller unmarshaller;
  private final Map<String, KrankenkasseninformationCtp> prefixToInformationMap;

  private DigaHealthInsuranceDirectoryFromXml(InputStream xmlMappingFileContent)
      throws JAXBException {
    context = JAXBContext.newInstance(KostentraegerMappingverzeichnis.class);
    unmarshaller = context.createUnmarshaller();
    var root = (KostentraegerMappingverzeichnis) unmarshaller.unmarshal(xmlMappingFileContent);
    prefixToInformationMap =
        root.getKrankenkasseninformation().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    KrankenkasseninformationCtp::getKostentraegerkuerzel, info -> info));
  }

  public static DigaHealthInsuranceDirectoryFromXml getInstance(InputStream xmlMappingFileContent)
      throws JAXBException {
    return new DigaHealthInsuranceDirectoryFromXml(xmlMappingFileContent);
  }

  @Override
  public KrankenkasseninformationCtp getInformation(String prefix) {
    return prefixToInformationMap.get(prefix);
  }
}

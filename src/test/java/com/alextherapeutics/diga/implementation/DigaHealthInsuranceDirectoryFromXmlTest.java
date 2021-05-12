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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DigaHealthInsuranceDirectoryFromXmlTest {
  private static final String sampleInsuranceCompanyMappingXml =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<n1:Kostentraeger_Mappingverzeichnis xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
          + "                                     xmlns:n1=\"http://www.gkv-datenaustausch.de/XML-Schema/EDRE0_MVZ/2.0.0\"\n"
          + "                                     xmlns:bas=\"http://www.gkv-datenaustausch.de/XMLSchema/EDFC0-basis/2.0.0\"\n"
          + "                                     version=\"002.000.000\" gueltigab=\"2020-07-01\" xsi:schemaLocation=\"\">\n"
          + "\t\t<n1:Krankenkasseninformation Nummer=\"ID1\">\n"
          + "\t\t\t\t<bas:Kostentraegerkuerzel>CH</bas:Kostentraegerkuerzel>\n"
          + "\t\t\t\t<bas:Kostentraegerkennung>109034270</bas:Kostentraegerkennung>\n"
          + "\t\t\t\t<bas:Name_des_Kostentraegers>BMW BKK   </bas:Name_des_Kostentraegers>\n"
          + "\t\t\t\t<bas:IK_des_Rechnungsempfaengers>109034270</bas:IK_des_Rechnungsempfaengers>\n"
          + "\t\t\t\t<bas:IK_Abrechnungsstelle>660500345</bas:IK_Abrechnungsstelle>\n"
          + "\t\t\t\t<bas:Name_Kommunikationsstelle>DIGA-BITMARCK</bas:Name_Kommunikationsstelle>\n"
          + "\t\t\t\t<bas:Endpunkt_Kommunikationsstelle>diga.bitmarck-daten.de</bas:Endpunkt_Kommunikationsstelle>\n"
          + "\t\t\t\t<bas:Versandart>1</bas:Versandart>\n"
          + "\t\t\t\t<bas:Postalische_Zusaetze>BMW BKK</bas:Postalische_Zusaetze>\n"
          + "\t\t\t\t<bas:Strasse_Postfach>Mengkofener Str.</bas:Strasse_Postfach>\n"
          + "\t\t\t\t<bas:Hausnummer_Postfachnummer>6</bas:Hausnummer_Postfachnummer>\n"
          + "\t\t\t\t<bas:PLZ>84130</bas:PLZ>\n"
          + "\t\t\t\t<bas:Ort>Dingolfing</bas:Ort>\n"
          + "\t\t\t\t<bas:Kontaktdaten_Technisch_Telefon>0800-24862725</bas:Kontaktdaten_Technisch_Telefon>\n"
          + "\t\t\t\t<bas:Kontaktdaten_Technisch_EMail>servicedesk@bitmarck.de</bas:Kontaktdaten_Technisch_EMail>\n"
          + "\t\t</n1:Krankenkasseninformation></n1:Kostentraeger_Mappingverzeichnis>";

  @Test
  void createsWithoutError() {
    assertDoesNotThrow(
        () ->
            DigaHealthInsuranceDirectoryFromXml.getInstance(
                new ByteArrayInputStream(
                    sampleInsuranceCompanyMappingXml.getBytes(StandardCharsets.UTF_8))));
  }
}

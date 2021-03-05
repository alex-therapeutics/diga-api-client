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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DigaXmlJaxbRequestReaderTest {
    private DigaXmlJaxbRequestReader reader;
    @BeforeEach
    void init() throws JAXBException {
        reader = new DigaXmlJaxbRequestReader();
    }
    @Test
    void testReadBillingReportCompletesWithoutErrors() throws JAXBException, IOException {
        var response = reader.readBillingReport(new ByteArrayInputStream(sampleBillingValidationReport.getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNotNull(response);
    }
    @Test
    void testReadCodeValidationResponseCompletesWithoutErrors() throws JAXBException, IOException {
        var response = reader.readCodeValidationResponse(new ByteArrayInputStream(sampleCodeValidationAnswer.getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNotNull(response);
    }
    private static String sampleCodeValidationAnswer =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Pruefung_Freischaltcode xmlns=\"http://www.gkv-datenaustausch.de/XML-Schema/EDFC0_Pruefung/2.0.0\"\n" +
                    "                         version=\"002.000.000\" gueltigab=\"2020-07-01\" verfahrenskennung=\"TDFC0\" nachrichtentyp=\"ANT\"\n" +
                    "                         absender=\"987654321\" empfaenger=\"123456789\">\n" +
                    "    <Antwort>\n" +
                    "        <IK_DiGA_Hersteller>123456789</IK_DiGA_Hersteller>\n" +
                    "        <IK_Krankenkasse>987654321</IK_Krankenkasse>\n" +
                    "        <DiGAVEID>12345000</DiGAVEID>\n" +
                    "        <Freischaltcode>ABCDEFGHIJKLMNOP</Freischaltcode>\n" +
                    "        <Tag_der_Leistungserbringung>2020-08-19</Tag_der_Leistungserbringung>\n" +
                    "    </Antwort>\n" +
                    "</Pruefung_Freischaltcode>\n";
    private static String sampleBillingValidationReport =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rep:report xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:rep=\"http://www.xoev.de/de/validator/varl/1\" xmlns:s=\"http://www.xoev.de/de/validator/framework/1/scenarios\" xmlns:in=\"http://www.xoev.de/de/validator/framework/1/createreportinput\" xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" xmlns:xd=\"http://www.oxygenxml.com/ns/doc/xsl\" xmlns:html=\"http://www.w3.org/1999/xhtml\" varlVersion=\"1.0.0\" valid=\"false\"><rep:engine><rep:name>KoSIT Validator 1.4.1</rep:name></rep:engine><rep:timestamp>2021-03-05T14:08:08.316+01:00</rep:timestamp><rep:documentIdentification><rep:documentHash><rep:hashAlgorithm>SHA-256</rep:hashAlgorithm><rep:hashValue>0Bd7PUeQMj6yMPomi+YKhleX/waBAFONbReNuLNr4Do=</rep:hashValue></rep:documentHash><rep:documentReference>StreamSource</rep:documentReference></rep:documentIdentification><rep:scenarioMatched><s:scenario><s:name>DiGA-Rechnung (DRE0-Anfrage) basierend auf EN16931 CIUS XRechnung (UN/CEFACT CII 100.D16B)</s:name><s:description><s:p>Voraussetzung für diese Prüfung ist, dass es sich um eine gültige XRechnung gemäß Version 1.2 oder 2.0                 handelt. Bitte verwenden Sie dafür im ersten Schritt die \"validator-configuration-xrechnung\" in der                 entsprechenden Version, bevor Sie diese Prüfung durchführen.</s:p></s:description><s:namespace prefix=\"ram\">urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100</s:namespace><s:namespace prefix=\"rep\">http://www.xoev.de/de/validator/varl/1</s:namespace><s:namespace prefix=\"rsm\">urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100</s:namespace><s:match>/rsm:CrossIndustryInvoice[starts-with(normalize-space(rsm:ExchangedDocumentContext/ram:GuidelineSpecifiedDocumentContextParameter/ram:ID),\n" +
                    "            'urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_')]</s:match><s:validateWithXmlSchema><s:resource><s:name>XML-Schema-Dummy für das Abrechnen von Freischaltcodes (basierend auf CII)</s:name><s:location>xml-schema/dre0/dummy-cii.xsd</s:location></s:resource></s:validateWithXmlSchema><s:validateWithSchematron><s:resource><s:name>Schematron-Regeln für das Abrechnen von Freischaltcodes</s:name><s:location>schematron/dre0.xsl</s:location></s:resource></s:validateWithSchematron><s:createReport><s:resource><s:name>Report für das Abrechnen von Freischaltcodes</s:name><s:location>report/dre0.xsl</s:location></s:resource></s:createReport><s:acceptMatch>/rep:report/rep:assessment[1]/rep:accept[1]</s:acceptMatch></s:scenario><rep:documentData xmlns:ubl=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\" xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\" xmlns:udt=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100\"><seller>MyDigaCompany</seller><id>1</id><issueDate>20210305</issueDate></rep:documentData><rep:validationStepResult id=\"val-xsd\" valid=\"true\"><s:resource><s:name>XML-Schema-Dummy für das Abrechnen von Freischaltcodes (basierend auf CII)</s:name><s:location>xml-schema/dre0/dummy-cii.xsd</s:location></s:resource></rep:validationStepResult><rep:validationStepResult id=\"val-sch.1\" valid=\"true\"><s:resource><s:name>Schematron-Regeln für das Abrechnen von Freischaltcodes</s:name><s:location>schematron/dre0.xsl</s:location></s:resource></rep:validationStepResult><rep:validationStepResult id=\"INVER\" valid=\"false\"><s:resource><s:name>Rechnungsprüfung</s:name><s:location>???</s:location></s:resource><rep:message xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\" id=\"INVER-1\" code=\"INVER-1\" level=\"error\" xpathLocation=\"rsm:CrossIndustryInvoice\">Anfrage oder Datei ungültig: NonEmptyList(de.bitmarck.bms.diga.activation.core.model.ModelError$InvalidBase32Check1: Base32 string \"CHAAAAAAAAAAAAAX\" does not end with \"J'.)</rep:message></rep:validationStepResult></rep:scenarioMatched><rep:assessment><rep:reject><rep:explanation><html xmlns=\"http://www.w3.org/1999/xhtml\" data-report-type=\"report\"><head><title>Prüfbericht</title><style>\n" +
                    "                body{\n" +
                    "                font-family: Calibri;\n" +
                    "                width: 230mm;\n" +
                    "                }\n" +
                    "                \n" +
                    "                .metadata dt {\n" +
                    "                float: left;\n" +
                    "                width: 230px;\n" +
                    "                clear: left;\n" +
                    "                }\n" +
                    "                \n" +
                    "                .metadata dd {\n" +
                    "                margin-left: 250px;\n" +
                    "                }\n" +
                    "                \n" +
                    "                table{\n" +
                    "                border-collapse: collapse;\n" +
                    "                width: 100%;\n" +
                    "                }\n" +
                    "                \n" +
                    "                table.tbl-errors{\n" +
                    "                font-size: smaller;\n" +
                    "                }\n" +
                    "               \n" +
                    "                table.document{\n" +
                    "                font-size: smaller;\n" +
                    "                }\n" +
                    "               \n" +
                    "                table.document td {vertical-align:top;}\n" +
                    "                \n" +
                    "                .tbl-errors td{\n" +
                    "                border: 1px solid lightgray;\n" +
                    "                padding: 2px;\n" +
                    "                vertical-align: top;\n" +
                    "                }\n" +
                    "                \n" +
                    "                thead{\n" +
                    "                font-weight: bold;\n" +
                    "                background-color: #f0f0f0;\n" +
                    "                padding-top: 6pt;\n" +
                    "                padding-bottom: 2pt;\n" +
                    "                }\n" +
                    "                \n" +
                    "                .tbl-meta td{\n" +
                    "                padding-right: 1em;\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.pos{\n" +
                    "                padding-left: 3pt;\n" +
                    "                width: 5%;\n" +
                    "                color: gray\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.element{\n" +
                    "                width: 95%;\n" +
                    "                word-wrap: break-word;\n" +
                    "                }\n" +
                    "                \n" +
                    "                \n" +
                    "                td.element:before{\n" +
                    "                content: attr(title);\n" +
                    "                color: gray;\n" +
                    "                }\n" +
                    "                \n" +
                    "                \n" +
                    "                div.attribute{\n" +
                    "                display: inline;\n" +
                    "                font-style: italic;\n" +
                    "                color: gray;\n" +
                    "                }\n" +
                    "                div.attribute:before{\n" +
                    "                content: attr(title) '=';\n" +
                    "                }\n" +
                    "                div.val{\n" +
                    "                display: inline;\n" +
                    "                font-weight: bold;\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.level1{\n" +
                    "                padding-left: 2mm;\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.level2{\n" +
                    "                padding-left: 5mm;\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.level3{\n" +
                    "                padding-left: 10mm;\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.level4{\n" +
                    "                padding-left: 15mm;\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.level5{\n" +
                    "                padding-left: 20mm;\n" +
                    "                }\n" +
                    "                td.level6{\n" +
                    "                padding-left: 25mm;\n" +
                    "                }\n" +
                    "                \n" +
                    "                tr{\n" +
                    "                vertical-align: bottom;\n" +
                    "                border-bottom: 1px solid #c0c0c0;\n" +
                    "                }\n" +
                    "                \n" +
                    "                .error{\n" +
                    "                color: red;\n" +
                    "                }\n" +
                    "                \n" +
                    "                .warning{\n" +
                    "                }\n" +
                    "                \n" +
                    "                p.important{\n" +
                    "                font-weight: bold;\n" +
                    "                text-align: left;\n" +
                    "                background-color: #e0e0e0;\n" +
                    "                padding: 3pt;\n" +
                    "                }\n" +
                    "                \n" +
                    "                td.right{\n" +
                    "                text-align: right\n" +
                    "                }</style></head><body><h1>Prüfbericht</h1><div class=\"metadata\"><p class=\"important\">Angaben zum geprüften Dokument</p><dl><dt>Referenz:</dt><dd>StreamSource</dd><dt>Zeitpunkt der Prüfung:</dt><dd>5.3.2021 14:08:08</dd><dt>Erkannter Dokumenttyp:</dt><dd>DiGA-Rechnung (DRE0-Anfrage) basierend auf EN16931 CIUS XRechnung (UN/CEFACT CII 100.D16B)</dd></dl><dl xmlns:ubl=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\" xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\" xmlns:udt=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100\"><dt>Erkannter Rechnungssteller:</dt><dd>MyDigaCompany</dd><dt>Erkannte Rechnungsnummer:</dt><dd>1</dd><dt>Erkanntes Rechnungsdatum:</dt><dd>20210305</dd></dl></div><p class=\"important\"><b>Konformitätsprüfung: </b>Das geprüfte Dokument enthält weder Fehler noch Warnungen. Es ist konform zu den formalen Vorgaben.</p><p class=\"important\">Bewertung: Es wird empfohlen das Dokument anzunehmen und weiter zu verarbeiten.</p><p class=\"important\">Inhalt des Dokuments:</p><table class=\"document\"><tr class=\"row\" id=\"0001\"><td class=\"pos\">0001</td><td class=\"element level1\" title=\"CrossIndustryInvoice\"><div class=\"val\">\n" +
                    "    </div><div class=\"val\">\n" +
                    "    </div><div class=\"val\">\n" +
                    "    </div><div class=\"val\">\n" +
                    "</div></td></tr><tr class=\"row\" id=\"0002\"><td class=\"pos\">0002</td><td class=\"element level2\" title=\"ExchangedDocumentContext\"><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "    </div></td></tr><tr class=\"row\" id=\"0003\"><td class=\"pos\">0003</td><td class=\"element level3\" title=\"GuidelineSpecifiedDocumentContextParameter\"><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "        </div></td></tr><tr class=\"row\" id=\"0004\"><td class=\"pos\">0004</td><td class=\"element level4\" title=\"ID\"><div class=\"val\">urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0</div></td></tr><tr class=\"row\" id=\"0005\"><td class=\"pos\">0005</td><td class=\"element level2\" title=\"ExchangedDocument\"><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "    </div></td></tr><tr class=\"row\" id=\"0006\"><td class=\"pos\">0006</td><td class=\"element level3\" title=\"ID\"><div class=\"val\">1</div></td></tr><tr class=\"row\" id=\"0007\"><td class=\"pos\">0007</td><td class=\"element level3\" title=\"TypeCode\"><div class=\"val\">380</div></td></tr><tr class=\"row\" id=\"0008\"><td class=\"pos\">0008</td><td class=\"element level3\" title=\"IssueDateTime\"><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "        </div></td></tr><tr class=\"row\" id=\"0009\"><td class=\"pos\">0009</td><td class=\"element level4\" title=\"DateTimeString\"><div class=\"val\">20210305</div><div class=\"attribute\" title=\"format\">102</div></td></tr><tr class=\"row\" id=\"0010\"><td class=\"pos\">0010</td><td class=\"element level2\" title=\"SupplyChainTradeTransaction\"><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "        </div><div class=\"val\">\n" +
                    "    </div></td></tr><tr class=\"row\" id=\"0011\"><td class=\"pos\">0011</td><td class=\"element level3\" title=\"IncludedSupplyChainTradeLineItem\"><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "        </div></td></tr><tr class=\"row\" id=\"0012\"><td class=\"pos\">0012</td><td class=\"element level4\" title=\"AssociatedDocumentLineDocument\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0013\"><td class=\"pos\">0013</td><td class=\"element level5\" title=\"LineID\"><div class=\"val\">1</div></td></tr><tr class=\"row\" id=\"0014\"><td class=\"pos\">0014</td><td class=\"element level4\" title=\"SpecifiedTradeProduct\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0015\"><td class=\"pos\">0015</td><td class=\"element level5\" title=\"GlobalID\"><div class=\"val\">12345000</div><div class=\"attribute\" title=\"schemeID\">DiGAVEID</div></td></tr><tr class=\"row\" id=\"0016\"><td class=\"pos\">0016</td><td class=\"element level5\" title=\"BuyerAssignedID\"><div class=\"val\">CHAAAAAAAAAAAAAX</div><div class=\"attribute\" title=\"schemeID\">Freischaltcode</div></td></tr><tr class=\"row\" id=\"0017\"><td class=\"pos\">0017</td><td class=\"element level5\" title=\"Name\"><div class=\"val\">MyDiga</div></td></tr><tr class=\"row\" id=\"0018\"><td class=\"pos\">0018</td><td class=\"element level5\" title=\"Description\"><div class=\"val\">A Diga prescription.</div></td></tr><tr class=\"row\" id=\"0019\"><td class=\"pos\">0019</td><td class=\"element level4\" title=\"SpecifiedLineTradeAgreement\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0020\"><td class=\"pos\">0020</td><td class=\"element level5\" title=\"NetPriceProductTradePrice\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0021\"><td class=\"pos\">0021</td><td class=\"element level6\" title=\"ChargeAmount\"><div class=\"val\">100</div></td></tr><tr class=\"row\" id=\"0022\"><td class=\"pos\">0022</td><td class=\"element level4\" title=\"SpecifiedLineTradeDelivery\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0023\"><td class=\"pos\">0023</td><td class=\"element level5\" title=\"BilledQuantity\"><div class=\"val\">1</div><div class=\"attribute\" title=\"unitCode\">C62</div></td></tr><tr class=\"row\" id=\"0024\"><td class=\"pos\">0024</td><td class=\"element level4\" title=\"SpecifiedLineTradeSettlement\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0025\"><td class=\"pos\">0025</td><td class=\"element level5\" title=\"ApplicableTradeTax\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0026\"><td class=\"pos\">0026</td><td class=\"element level6\" title=\"TypeCode\"><div class=\"val\">VAT</div></td></tr><tr class=\"row\" id=\"0027\"><td class=\"pos\">0027</td><td class=\"element level6\" title=\"CategoryCode\"><div class=\"val\">S</div></td></tr><tr class=\"row\" id=\"0028\"><td class=\"pos\">0028</td><td class=\"element level6\" title=\"RateApplicablePercent\"><div class=\"val\">19</div></td></tr><tr class=\"row\" id=\"0029\"><td class=\"pos\">0029</td><td class=\"element level5\" title=\"SpecifiedTradeSettlementLineMonetarySummation\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0030\"><td class=\"pos\">0030</td><td class=\"element level6\" title=\"LineTotalAmount\"><div class=\"val\">100</div></td></tr><tr class=\"row\" id=\"0031\"><td class=\"pos\">0031</td><td class=\"element level3\" title=\"ApplicableHeaderTradeAgreement\"><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "        </div></td></tr><tr class=\"row\" id=\"0032\"><td class=\"pos\">0032</td><td class=\"element level4\" title=\"BuyerReference\"><div class=\"val\">Leitweg-ID</div></td></tr><tr class=\"row\" id=\"0033\"><td class=\"pos\">0033</td><td class=\"element level4\" title=\"SellerTradeParty\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0034\"><td class=\"pos\">0034</td><td class=\"element level5\" title=\"ID\"><div class=\"val\">MyDigaCompany AB</div></td></tr><tr class=\"row\" id=\"0035\"><td class=\"pos\">0035</td><td class=\"element level5\" title=\"ID\"><div class=\"val\">580000044</div><div class=\"attribute\" title=\"schemeID\">IK</div></td></tr><tr class=\"row\" id=\"0036\"><td class=\"pos\">0036</td><td class=\"element level5\" title=\"Name\"><div class=\"val\">MyDigaCompany</div></td></tr><tr class=\"row\" id=\"0037\"><td class=\"pos\">0037</td><td class=\"element level5\" title=\"DefinedTradeContact\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0038\"><td class=\"pos\">0038</td><td class=\"element level6\" title=\"PersonName\"><div class=\"val\">Sven Svensson</div></td></tr><tr class=\"row\" id=\"0039\"><td class=\"pos\">0039</td><td class=\"element level6\" title=\"TelephoneUniversalCommunication\"><div class=\"val\">\n" +
                    "                        </div><div class=\"val\">\n" +
                    "                    </div></td></tr><tr class=\"row\" id=\"0040\"><td class=\"pos\">0040</td><td class=\"element level7\" title=\"CompleteNumber\"><div class=\"val\">+46 70 123 45 67</div></td></tr><tr class=\"row\" id=\"0041\"><td class=\"pos\">0041</td><td class=\"element level6\" title=\"EmailURIUniversalCommunication\"><div class=\"val\">\n" +
                    "                        </div><div class=\"val\">\n" +
                    "                    </div></td></tr><tr class=\"row\" id=\"0042\"><td class=\"pos\">0042</td><td class=\"element level7\" title=\"URIID\"><div class=\"val\">diga@diga.com</div></td></tr><tr class=\"row\" id=\"0043\"><td class=\"pos\">0043</td><td class=\"element level5\" title=\"PostalTradeAddress\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0044\"><td class=\"pos\">0044</td><td class=\"element level6\" title=\"PostcodeCode\"><div class=\"val\">123 45</div></td></tr><tr class=\"row\" id=\"0045\"><td class=\"pos\">0045</td><td class=\"element level6\" title=\"LineOne\"><div class=\"val\">Test Street 1</div></td></tr><tr class=\"row\" id=\"0046\"><td class=\"pos\">0046</td><td class=\"element level6\" title=\"CityName\"><div class=\"val\">Stockholm</div></td></tr><tr class=\"row\" id=\"0047\"><td class=\"pos\">0047</td><td class=\"element level6\" title=\"CountryID\"><div class=\"val\">SE</div></td></tr><tr class=\"row\" id=\"0048\"><td class=\"pos\">0048</td><td class=\"element level5\" title=\"SpecifiedTaxRegistration\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0049\"><td class=\"pos\">0049</td><td class=\"element level6\" title=\"ID\"><div class=\"val\">SE1234567890</div><div class=\"attribute\" title=\"schemeID\">VA</div></td></tr><tr class=\"row\" id=\"0050\"><td class=\"pos\">0050</td><td class=\"element level4\" title=\"BuyerTradeParty\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0051\"><td class=\"pos\">0051</td><td class=\"element level5\" title=\"ID\"><div class=\"val\">BMW BKK   </div></td></tr><tr class=\"row\" id=\"0052\"><td class=\"pos\">0052</td><td class=\"element level5\" title=\"ID\"><div class=\"val\">660500345</div><div class=\"attribute\" title=\"schemeID\">IK</div></td></tr><tr class=\"row\" id=\"0053\"><td class=\"pos\">0053</td><td class=\"element level5\" title=\"Name\"><div class=\"val\">BMW BKK   </div></td></tr><tr class=\"row\" id=\"0054\"><td class=\"pos\">0054</td><td class=\"element level5\" title=\"PostalTradeAddress\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0055\"><td class=\"pos\">0055</td><td class=\"element level6\" title=\"PostcodeCode\"><div class=\"val\">84130</div></td></tr><tr class=\"row\" id=\"0056\"><td class=\"pos\">0056</td><td class=\"element level6\" title=\"LineOne\"><div class=\"val\">Mengkofener Str. 6</div></td></tr><tr class=\"row\" id=\"0057\"><td class=\"pos\">0057</td><td class=\"element level6\" title=\"CityName\"><div class=\"val\">Dingolfing</div></td></tr><tr class=\"row\" id=\"0058\"><td class=\"pos\">0058</td><td class=\"element level6\" title=\"CountryID\"><div class=\"val\">DE</div></td></tr><tr class=\"row\" id=\"0059\"><td class=\"pos\">0059</td><td class=\"element level3\" title=\"ApplicableHeaderTradeDelivery\"><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "        </div></td></tr><tr class=\"row\" id=\"0060\"><td class=\"pos\">0060</td><td class=\"element level4\" title=\"ActualDeliverySupplyChainEvent\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0061\"><td class=\"pos\">0061</td><td class=\"element level5\" title=\"OccurrenceDateTime\"><div class=\"val\">\n" +
                    "                    </div><div class=\"val\">\n" +
                    "                </div></td></tr><tr class=\"row\" id=\"0062\"><td class=\"pos\">0062</td><td class=\"element level6\" title=\"DateTimeString\"><div class=\"val\">20210305</div><div class=\"attribute\" title=\"format\">102</div></td></tr><tr class=\"row\" id=\"0063\"><td class=\"pos\">0063</td><td class=\"element level3\" title=\"ApplicableHeaderTradeSettlement\"><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "            </div><div class=\"val\">\n" +
                    "        </div></td></tr><tr class=\"row\" id=\"0064\"><td class=\"pos\">0064</td><td class=\"element level4\" title=\"CreditorReferenceID\"><div class=\"val\">109034270</div><div class=\"attribute\" title=\"schemeID\">IK</div></td></tr><tr class=\"row\" id=\"0065\"><td class=\"pos\">0065</td><td class=\"element level4\" title=\"InvoiceCurrencyCode\"><div class=\"val\">EUR</div></td></tr><tr class=\"row\" id=\"0066\"><td class=\"pos\">0066</td><td class=\"element level4\" title=\"SpecifiedTradeSettlementPaymentMeans\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0067\"><td class=\"pos\">0067</td><td class=\"element level5\" title=\"TypeCode\"><div class=\"val\">30</div></td></tr><tr class=\"row\" id=\"0068\"><td class=\"pos\">0068</td><td class=\"element level4\" title=\"ApplicableTradeTax\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0069\"><td class=\"pos\">0069</td><td class=\"element level5\" title=\"CalculatedAmount\"><div class=\"val\">19.00</div></td></tr><tr class=\"row\" id=\"0070\"><td class=\"pos\">0070</td><td class=\"element level5\" title=\"TypeCode\"><div class=\"val\">VAT</div></td></tr><tr class=\"row\" id=\"0071\"><td class=\"pos\">0071</td><td class=\"element level5\" title=\"BasisAmount\"><div class=\"val\">100</div></td></tr><tr class=\"row\" id=\"0072\"><td class=\"pos\">0072</td><td class=\"element level5\" title=\"CategoryCode\"><div class=\"val\">S</div></td></tr><tr class=\"row\" id=\"0073\"><td class=\"pos\">0073</td><td class=\"element level5\" title=\"RateApplicablePercent\"><div class=\"val\">19</div></td></tr><tr class=\"row\" id=\"0074\"><td class=\"pos\">0074</td><td class=\"element level4\" title=\"SpecifiedTradePaymentTerms\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0075\"><td class=\"pos\">0075</td><td class=\"element level5\" title=\"Description\"/></tr><tr class=\"row\" id=\"0076\"><td class=\"pos\">0076</td><td class=\"element level4\" title=\"SpecifiedTradeSettlementHeaderMonetarySummation\"><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "                </div><div class=\"val\">\n" +
                    "            </div></td></tr><tr class=\"row\" id=\"0077\"><td class=\"pos\">0077</td><td class=\"element level5\" title=\"LineTotalAmount\"><div class=\"val\">100</div></td></tr><tr class=\"row\" id=\"0078\"><td class=\"pos\">0078</td><td class=\"element level5\" title=\"TaxBasisTotalAmount\"><div class=\"val\">100</div></td></tr><tr class=\"row\" id=\"0079\"><td class=\"pos\">0079</td><td class=\"element level5\" title=\"TaxTotalAmount\"><div class=\"val\">19</div><div class=\"attribute\" title=\"currencyID\">EUR</div></td></tr><tr class=\"row\" id=\"0080\"><td class=\"pos\">0080</td><td class=\"element level5\" title=\"GrandTotalAmount\"><div class=\"val\">119.00</div></td></tr><tr class=\"row\" id=\"0081\"><td class=\"pos\">0081</td><td class=\"element level5\" title=\"DuePayableAmount\"><div class=\"val\">119.00</div></td></tr></table><p class=\"info\">Dieser Prüfbericht wurde erstellt mit KoSIT Validator 1.4.1.</p></body></html></rep:explanation></rep:reject></rep:assessment></rep:report>";

}
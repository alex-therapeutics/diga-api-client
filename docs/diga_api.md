# DiGA API documentation

This documents contains information for DiGA manufacturers on how the DiGA API works regarding prescription code validation and billing.
It is targeted at manufacturers who wish to develop their own integration with the DiGA API or are curious to know how it works.
If you are looking for an out-of-the-box solution that already works, you can use the [diga api client](https://github.com/alex-therapeutics/diga-api-client) provided in this repository as a java library, or you can use the [diga api service](https://github.com/gtuk/diga-api-service) (work in progress), which is an api wrapper around this library, as a microservice in your stack.
Information on how to use these solutions is provided in their readmes and code documentation, and not in this document.

- [DiGA API documentation](#diga-api-documentation)
- [Overview](#overview)
- [Summary](#summary)
- [The prescription code](#the-prescription-code)
  - [Mapping file for insurance companies](#mapping-file-for-insurance-companies)
    - [IK numbers](#ik-numbers)
- [Request format](#request-format)
  - [Institutionskennzeichen - IK numbers](#institutionskennzeichen---ik-numbers)
    - [Requesting an IK number](#requesting-an-ik-number)
  - [Verfahren](#verfahren)
  - [Nutzdaten](#nutzdaten)
- [Request details](#request-details)
  - [Code verification requests](#code-verification-requests)
  - [Billing requests](#billing-requests)
  - [Encryption](#encryption)
    - [Useful Links](#useful-links)
  - [Auditing](#auditing)
- [FAQ](#faq)
  - [What can I do if I want to make requests but my DiGA is not approved yet and I therefore do not have a diga id?](#what-can-i-do-if-i-want-to-make-requests-but-my-diga-is-not-approved-yet-and-i-therefore-do-not-have-a-diga-id)
  - [What can I do if I dont know the IK number yet and still want to test the client?](#what-can-i-do-if-i-dont-know-the-ik-number-yet-and-still-want-to-test-the-client)
  - [Are there solutions for non-java users?](#are-there-solutions-for-non-java-users)
  - [My DiGA (app) is only valid for 30 days. Can users request a fresh code from their insurer while their existing code is still valid?](#my-diga-app-is-only-valid-for-30-days-can-users-request-a-fresh-code-from-their-insurer-while-their-existing-code-is-still-valid)
  - [What are expected response times for the requests?](#what-are-expected-response-times-for-the-requests)
  - [How can we handle error responses with codes 201 or 202?](#how-can-we-handle-error-responses-with-codes-201-or-202)
- [Glossary](#glossary)

# Overview

Manufacturers of digitial health applications (DiGA) can apply for a fast track at [bfarm](https://www.bfarm.de/EN/MedicalDevices/DiGA/_node.html) to place their solutions in a directory of apps which can be prescribed by doctors to patients.
By showing evidence that the apps improve patients' health and by obtaining medical device certifications, it can be assured that the apps are of high quality.
On the other side, DiGA manufacturers get reimbursed by health insurance companies when their apps are prescribed to patients.
The prescription process is built around a 16-character prescription code which can be used to activate the apps but which is also required to send the invoices.
This repository contains code to:

- verify that user-entered prescription codes are valid
- create and send invoices to insurer based on prescription codes.

This documentation complements the code by describing the DiGA api and the full flow on a higher level.
With this documentation we hope that other DiGA manufacturers can understand the required steps to verify and reimburse prescription codes without having to read through the (mainly German) official documentation.
Also we hope that it will facilitate writing similar solutions in other programming languages if necessary.
Finally, it can be used as a place to collect questions and answers around the DiGA api.

# Summary

- user enters prescription code in the DiGA (app)
- insurer information is extracted from the code
- a request is made to the api of the insurer to verify the code. The response includes two fields which are required for the billing:
  - `Tag der Leistungserbringung` = the day a user started to use the app
  - `digaVEID` = diga id + 3 digits
- another request is made for reimbursing the code

# The prescription code

Patients receive 16-character prescription codes from their insurance company.
The codes are used to activate the DiGA apps and for creating invoices for billing.
The structure of the code is defined in the attachment [Technische Anlage für die elektronische Abrechnung der digitalen Gesundheitsanwendungen nach § 33a SGB V (elektronische Datenübermittlung) V1.2 (PDF)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_Anlage_1_Technische_Anlage_zur_RL_V1.1_20210225.pdf) on page 9. ([Overview Page](https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp))
The code contains only capitalized characters and digits from: `[A-Z; 2-7]`.

| Insurance ID - Krankenkassencode/Kostenträgerkürzel | Version     | Unique code for insurer | Checksum    |
| --------------------------------------------------- | ----------- | ----------------------- | ----------- |
| 2 characters                                        | 1 character | 12 characters           | 1 character |

The first two characters are an identifier for the insurer.
They can be used to get additional information from a mapping (xml) file which can be downloaded [here](https://kkv.gkv-diga.de/).
Therefore, the code is **case-sensitive**.

The other parts contain a version (`A`) and a unique code for the patient & insurer.
The last character is a checksum which is described in [Anhang 3 - Freischaltcode / Rezeptcode – Berechnungsregel Prüfziffer (PDF)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/Anhang3_Freischaltcode_Berechnungsregel_Pruefziffer_V1.0.pdf) with an open source implementation for different languages on [Github](https://github.com/bitmarck-service).

Codes for testing can be downloaded from [Anhang 6 - DiGA-Pseudocodes (XLSX)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/Pseudo-Codes.xlsx).
Some of these codes are invalid with error codes defined in [Anhang 5 - Fehlerausgaben (PDF)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_Anhang5_Fehlerausgaben_V1.0.1_20210423.pdf). A translated table can be found below

| Rückgabewert | Freischaltcode   | Fehlertext              | Erläuterung                                                                                                                                              |
| ------------ | ---------------- | ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 0            | 77AAAAAAAAAAAAAX | Request valid           | Use current date for "Tag_der_Leistungserbringung" from EDFC0-basis_2.0.0.xsd. For the DiGANr from EDFC0-basis_2.0.0.xsd the last 3 digits must be `000` |
| 100          | 77AAAAAAAAAAADEV | Code expired            | The code is expired.                                                                                                                                     |
| 101          | 77AAAAAAAAAAADFF | Code canceled           | "Fachlicher Fehler." - very unspecific - might be invalidated by insurer?                                                                                |
| 102          | 77AAAAAAAAAAADGE | Code not found          | E.g. code was not distributed by the insurer                                                                                                             |
| 200          | 77AAAAAAAAAAAGIS | Request or file invalid | The request or file might be invalid so they cannot be processed, e.g. schema error                                                                      |
| 201          | 77AAAAAAAAAAAGJC | Server error            | Technical Error, e.g. network issue                                                                                                                      |
| 202          | 77AAAAAAAAAAAGKD | Memory error            | Technical Error, e.g. database error                                                                                                                     |

## Mapping file for insurance companies

When receiving a patient's code you need to find out where to send it and which data to send in the request.
For this, you should use the insurance company mapping file from [here](https://kkv.gkv-diga.de/).
Data for one insurer is listed below where the `Kostentraegerkuerzel` should match the first two characters of the prescription code.

```xml
<n1:Krankenkasseninformation Nummer="ID1">
        <bas:Kostentraegerkuerzel>CH</bas:Kostentraegerkuerzel>
        <bas:Kostentraegerkennung>109034270</bas:Kostentraegerkennung>
        <bas:Name_des_Kostentraegers>BMW BKK   </bas:Name_des_Kostentraegers>
        <bas:IK_des_Rechnungsempfaengers>109034270</bas:IK_des_Rechnungsempfaengers>
        <bas:IK_Abrechnungsstelle>660500345</bas:IK_Abrechnungsstelle>
        <bas:Name_Kommunikationsstelle>DIGA-BITMARCK</bas:Name_Kommunikationsstelle>
        <bas:Endpunkt_Kommunikationsstelle>diga.bitmarck-daten.de</bas:Endpunkt_Kommunikationsstelle>
        <bas:Versandart>1</bas:Versandart>
        <bas:Postalische_Zusaetze>BMW BKK</bas:Postalische_Zusaetze>
        <bas:Strasse_Postfach>Mengkofener Str.</bas:Strasse_Postfach>
        <bas:Hausnummer_Postfachnummer>6</bas:Hausnummer_Postfachnummer>
        <bas:PLZ>84130</bas:PLZ>
        <bas:Ort>Dingolfing</bas:Ort>
        <bas:Kontaktdaten_Technisch_Telefon>0800-24862725</bas:Kontaktdaten_Technisch_Telefon>
        <bas:Kontaktdaten_Technisch_EMail>servicedesk@bitmarck.de</bas:Kontaktdaten_Technisch_EMail>
</n1:Krankenkasseninformation>
```

The file can be updated quarterly with every new round of DiGA application deadlines by insurance companies which requires keeping the mapping file used for the client up-to-date.
The field `Kostentraegerkuerzel` is what can be used to find insurer information based on the prescription code.
The information lists a field `Endpunkt_Kommunikationsstelle`.
This is the base url for the endpoint for a specific insurer.
There is no central DiGA endpoint for verifying and reimbursing DiGA apps and handling might differ across apis.
Some apis might also not support billing as of now.
The `Versandart` field in the mapping file can be used to check this beforehand:

- 1 = api is supported for billing
- 2 = email (none of the insurances in the mapping file use emails)
- 3 = post

Although insurance companies use their own apis, they do follow an openapi specification for the request which is listed as [DiGA-YAML-Datei (YAML)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/digaSP_1_0_05.yaml) at the same [gkv page](https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp).
This [document](https://github.com/alex-therapeutics/diga-api-client/blob/main/ENDPOINT_STATUS.md) lists which apis are currently working with the diga api client.

The mapping file also contains billing-related information about the insurance companies as well as different IK numbers.
The following section describes what the different IK numbers are used for as this might not be obvious from the documentation.

### IK numbers

The xml file contains the following rows:

```xml
<bas:Kostentraegerkennung>109034270</bas:Kostentraegerkennung>
<bas:IK_des_Rechnungsempfaengers>109034270</bas:IK_des_Rechnungsempfaengers>
<bas:IK_Abrechnungsstelle>660500345</bas:IK_Abrechnungsstelle>
```

The values for `Kostentraegerkennung` and `IK_des_Rechnungsempfaengers` are the same in this case (not always true) but the `IK_Abrechnungsstelle` is different.
The IK number `660500345` is the IK number of the api provider, here bitmarck and the other IK number relates to the insurance company.
When making requests it is important to use the correct IK numbers in the request header or in the `nutzdaten` field.

| xml field name              | name in this repository  | where to use                                                                            |
| --------------------------- | ------------------------ | --------------------------------------------------------------------------------------- |
| Kostentraegerkennung        | insuranceCompanyIKNumber | used as `empfaenger` and `IK_Krankenkasse` in XML file for code validation request      |
| IK_des_Rechnungsempfaengers | buyerCompanyCreditorIk   | used in billing request                                                                 |
| IK_Abrechnungsstelle        | clearingCenterIKNumber   | used in the request body as `ikempfaenger` & relevant for the encryption of `nutzdaten` |

# Request format

According to the openapi specification the request contains 4 different parameters which are sent with `ContentType: multipart/form-data`:

- `iksender` - Institutionskennzeichen (IK) of the DiGA manufacturer (an id required for payments)
- `ikempfaenger` - IK of the api service provider, i.e. `IK_Abrechnungsstelle`
- `verfahren` - a code which discriminates requests for code verification and reimbursement
  - values are described in a [later section](#verfahren)
- `nutzdaten` - additional information for the request, including the prescription code
  - the `nutzdaten` are described later in more detail as this is the tricky bit of the request

## Institutionskennzeichen - IK numbers

Detailed information about the IK numbers can be found on the gkv page [Sonstige Leistungserbringer](https://www.gkv-datenaustausch.de/leistungserbringer/sonstige_leistungserbringer/sonstige_leistungserbringer.jsp) in the file [Gemeinsames Rundschreiben Institutionskennzeichen (PDF)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/GR_IK_2020-06-01.pdf) (German only).

> Das IK ist ein eindeutiges Merkmal für die Abrechnung medizinischer und rehabilitativer Leistungen mit den Trägern der Sozialversicherung [...]

IK numbers are unique identifiers, 9 digits long, required for the billing of medical services and used as official identifiers.
Therefore, every DiGA manufacturer must request an IK number.
An IK number is also required when [requesting a certificate from ITSG](https://www.itsg.de/produkte/trust-center/zertifikat-beantragen/) which is required for encrypting and decrypting the `nutzdaten` of the request.
IK numbers might be updated when the name, address, or billing address of a company changes.

The linked document also contains more information about how the numbers are structured and more importantly, how they can be requested.

### Requesting an IK number

According to section 2.2.1 from [Gemeinsames Rundschreiben Institutionskennzeichen (PDF)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/GR_IK_2020-06-01.pdf)

> Die Vergabe, die Änderung der Daten [...] erfolgen auf Antrag. Der Erfassungsbeleg kann bei der ARGE•IK sowie bei jedem Sozialversicherungsträger angefordert oder auf der Internetseite der ARGE•IK (www.arge-ik.de) heruntergeladen werden.

You need to request the IK or changes using a form.
You can check the [ARGE-IK](https://www.dguv.de/arge-ik/index.jsp) website for more information (German).
Specific information for requesting the number can be found in the [Antrag](https://www.dguv.de/arge-ik/antrag/index.jsp) tab.
The form must be sent by email to `info@arge-ik.de` and a blueprint can be downloaded [here](https://www.dguv.de/medien/arge-ik/downloads/erfass.pdf).

Additional information at [gkv page](https://www.gkv-datenaustausch.de/leistungserbringer/sonstige_leistungserbringer/sonstige_leistungserbringer.jsp) can be found in [20210401_Broschuere_TP5 (PDF)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/sonstige_leistungserbringer/20210401_Broschuere_TP5.pdf)
which also contains a section about requesting certificates from ITSG trust center.

Based on our experience:
Once the ARGE-IK received the application they sent another document to be signed back via mail which took 8 working days to arrive.
After we filled out this document we received our official IK, again via mail, which took another 8 working days from the day we sent the letter.
Therefore, we recommend to request the IK number as early as possible.

## Verfahren

The `verfahren` parameter consists of 5 characters describing the kind of the request.
The first character is either `T` or `E` denoting `Testdaten` (test data) or `Echtdaten` (real data) - [attachment 4 - Verfahrenskennungen](https://www.gkv-datenaustausch.de/media/dokumente/standards_und_normen/technische_spezifikationen/Anlage_4_-_Verfahrenskennungen.pdf) on [gkv technische standards page](https://www.gkv-datenaustausch.de/technische_standards_1/technische_standards.jsp).

The next 3 characters are used to discriminate between:

- verification - DFC (**D**iga **F**reischalt**c**ode)
- billing - DRE (**D**iga **Re**chnung).

The last one is for versioning which leaves us with the following values:

- `EDFC0` and `TDFC0` for verification
- `EDRE0` and `TDRE0` for billing.

## Nutzdaten

`nutzdaten` is the tricky field which contains verification or billing related information based on the `verfahren` value.
`nutzdaten` are defined in an xml sheet which needs to be encrypted and encoded as byte array before sending the actual requests.
The structure of the xml files are defined by `xsd` files which can be found in `src/main/resources` or downloaded from [gkv page](https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp) as [attachment - DiGA-Abrechnungsschema (XSD) (ZIP)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_XSD_20201029.zip).
Examples for the data and additional details on the encryption are described in the following.

# Request details

## Code verification requests

See `EDFC0-Pruefung_2.0.0.xsd` file for reference:

Assuming request is sent to insurer with following details from mapping file and own IK number `000000000`:

```xml
<bas:Kostentraegerkennung>111111111</bas:Kostentraegerkennung>
<bas:IK_des_Rechnungsempfaengers>222222222</bas:IK_des_Rechnungsempfaengers>
<bas:IK_Abrechnungsstelle>333333333</bas:IK_Abrechnungsstelle>
```

Request headers:

- ikempfaenger="333333333"
- iksender="000000000"

Data for sending the request (`nutzdaten` before encryption):

```xml
"<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Pruefung_Freischaltcode xmlns="http://www.gkv-datenaustausch.de/XML-Schema/EDFC0_Pruefung/2.0.0"
                          version="002.000.000" gueltigab="2020-07-01" verfahrenskennung="TDFC0" nachrichtentyp="ANF"
                          absender="000000000" empfaenger="111111111">
    <Anfrage>
        <IK_DiGA_Hersteller>000000000</IK_DiGA_Hersteller>
        <IK_Krankenkasse>111111111</IK_Krankenkasse>
        <DiGAID>12345</DiGAID>
        <Freischaltcode>77AAAAAAAAAAAAAX</Freischaltcode>
    </Anfrage>
</Pruefung_Freischaltcode>"
```

In case you are not an approved DiGA manufacturer yet, you can put `12345` as `DiGAID`.
You should already have an `IK` number though as otherwise, you also wouldn't have the certificate for encryption/decryption.

Successful response data:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Pruefung_Freischaltcode xmlns="http://www.gkv-datenaustausch.de/XML-Schema/EDFC0_Pruefung/2.0.0"
                         version="002.000.000" gueltigab="2020-07-01" verfahrenskennung="TDFC0" nachrichtentyp="ANT"
                         absender="333333333" empfaenger="000000000">
    <Antwort>
        <IK_DiGA_Hersteller>000000000</IK_DiGA_Hersteller>
        <IK_Krankenkasse>111111111</IK_Krankenkasse>
        <DiGAVEID>12345000</DiGAVEID>
        <Freischaltcode>ABCDEFGHIJKLMNOP</Freischaltcode>
        <Tag_der_Leistungserbringung>2020-08-19</Tag_der_Leistungserbringung>
    </Antwort>
</Pruefung_Freischaltcode>
```

The response differs from the request by having `nachrichtentyp="ANT"` as header (`Antwort`) instead of `nachrichtentyp="ANF"` (`Anfrage`).
Also the diga id is listed as `DiGAVEID` (verified ID) and an additional field `Tag_der_Leistungserbringung` is returned (day when the service started).

## Billing requests

Invoices must be created using the `XRechnung` standard and for each prescription code, exactly one invoice must be sent.
There might also be dedicated accounting/book-keeping software which allows creating and sending invoices according to this standard, so that billing might also be handled outside the production system.
However, this can be a lot of work compared to sending invoices using this client.
When sending invoices with this client, it is up to the users of the client to define what should happen with the response from the diga api.
E.g. invoices are likely to be required for accounting and therefore must be stored.
Also handling of apis which do not support billing yet, must be defined.

The invoice should contain the following information:

- prescription code
- DiGA id of the manufacturer
- time period for prescription
- information about the invoice issuer and the insurer

Additional information can be found on this [wiki page](https://github.com/alex-therapeutics/diga-api-client/wiki/Billing-validation---XRechnung).

Assuming request is sent to insurer with following details from mapping file and own IK number `000000000`:

```xml
<bas:Kostentraegerkennung>111111111</bas:Kostentraegerkennung>
<bas:IK_des_Rechnungsempfaengers>222222222</bas:IK_des_Rechnungsempfaengers>
<bas:IK_Abrechnungsstelle>333333333</bas:IK_Abrechnungsstelle>
```

Request headers:

- ikempfaenger="333333333"
- iksender="000000000"

<details><summary>Data for sending the request (`nutzdaten` before encryption):</summary>

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns3:CrossIndustryInvoice xmlns="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
                          xmlns:ns2="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100"
                          xmlns:ns3="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
                          xmlns:ns4="urn:un:unece:uncefact:data:standard:QualifiedDataType:100">
    <ns3:ExchangedDocumentContext>
        <GuidelineSpecifiedDocumentContextParameter>
            <ID>urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0</ID>
        </GuidelineSpecifiedDocumentContextParameter>
    </ns3:ExchangedDocumentContext>
    <ns3:ExchangedDocument>
        <ID>1</ID>
        <TypeCode>380</TypeCode>
        <IssueDateTime>
            <ns2:DateTimeString format="102">20210722</ns2:DateTimeString>
        </IssueDateTime>
    </ns3:ExchangedDocument>
    <ns3:SupplyChainTradeTransaction>
        <IncludedSupplyChainTradeLineItem>
            <AssociatedDocumentLineDocument>
                <LineID>1</LineID>
            </AssociatedDocumentLineDocument>
            <SpecifiedTradeProduct>
                <GlobalID schemeID="DiGAVEID" />
                <BuyerAssignedID schemeID="Freischaltcode">VALID</BuyerAssignedID>
                <Name>digaName</Name>
                <Description>A digaName prescription.</Description>
            </SpecifiedTradeProduct>
            <SpecifiedLineTradeAgreement>
                <NetPriceProductTradePrice>
                    <ChargeAmount>100</ChargeAmount>
                </NetPriceProductTradePrice>
            </SpecifiedLineTradeAgreement>
            <SpecifiedLineTradeDelivery>
                <BilledQuantity unitCode="C62">1</BilledQuantity>
            </SpecifiedLineTradeDelivery>
            <SpecifiedLineTradeSettlement>
                <ApplicableTradeTax>
                    <TypeCode>VAT</TypeCode>
                    <CategoryCode>S</CategoryCode>
                    <RateApplicablePercent>19</RateApplicablePercent>
                </ApplicableTradeTax>
                <SpecifiedTradeSettlementLineMonetarySummation>
                    <LineTotalAmount>100</LineTotalAmount>
                </SpecifiedTradeSettlementLineMonetarySummation>
            </SpecifiedLineTradeSettlement>
        </IncludedSupplyChainTradeLineItem>
        <ApplicableHeaderTradeAgreement>
            <BuyerReference>Leitweg-ID</BuyerReference>
            <SellerTradeParty>
                <ID>IK000000000</ID>
                <ID schemeID="IK">000000000</ID>
                <Name>manufacturingCompanyName</Name>
                <DefinedTradeContact>
                    <PersonName>fullName</PersonName>
                    <TelephoneUniversalCommunication>
                        <CompleteNumber>phoneNumber</CompleteNumber>
                    </TelephoneUniversalCommunication>
                    <EmailURIUniversalCommunication>
                        <URIID>svensvensson@awesomedigacompany.com</URIID>
                    </EmailURIUniversalCommunication>
                </DefinedTradeContact>
                <PostalTradeAddress>
                    <PostcodeCode>postalCode</PostcodeCode>
                    <LineOne>adressLine</LineOne>
                    <CityName>city</CityName>
                    <CountryID>countryCode</CountryID>
                </PostalTradeAddress>
                <SpecifiedTaxRegistration>
                    <ID schemeID="VA">manufacturingCompanyVATRegistration</ID>
                </SpecifiedTaxRegistration>
            </SellerTradeParty>
            <BuyerTradeParty>
                <ID>IK111111111</ID>
                <ID schemeID="IK">111111111</ID>
                <Name>BMW BKK</Name>
                <PostalTradeAddress>
                    <PostcodeCode>84130</PostcodeCode>
                    <LineOne>Mengkofener Str. 6</LineOne>
                    <CityName>Dingolfing</CityName>
                    <CountryID>DE</CountryID>
                </PostalTradeAddress>
            </BuyerTradeParty>
        </ApplicableHeaderTradeAgreement>
        <ApplicableHeaderTradeDelivery>
            <ActualDeliverySupplyChainEvent>
                <OccurrenceDateTime>
                    <ns2:DateTimeString format="102">20210722</ns2:DateTimeString>
                </OccurrenceDateTime>
            </ActualDeliverySupplyChainEvent>
        </ApplicableHeaderTradeDelivery>
        <ApplicableHeaderTradeSettlement>
            <CreditorReferenceID schemeID="IK">000000000</CreditorReferenceID>
            <InvoiceCurrencyCode>EUR</InvoiceCurrencyCode>
            <SpecifiedTradeSettlementPaymentMeans>
                <TypeCode>30</TypeCode>
            </SpecifiedTradeSettlementPaymentMeans>
            <ApplicableTradeTax>
                <CalculatedAmount>19.00</CalculatedAmount>
                <TypeCode>VAT</TypeCode>
                <BasisAmount>100.00</BasisAmount>
                <CategoryCode>S</CategoryCode>
                <RateApplicablePercent>19</RateApplicablePercent>
            </ApplicableTradeTax>
            <SpecifiedTradePaymentTerms>
                <Description></Description>
            </SpecifiedTradePaymentTerms>
            <SpecifiedTradeSettlementHeaderMonetarySummation>
                <LineTotalAmount>100.00</LineTotalAmount>
                <TaxBasisTotalAmount>100.00</TaxBasisTotalAmount>
                <TaxTotalAmount currencyID="EUR">19.00</TaxTotalAmount>
                <GrandTotalAmount>119.00</GrandTotalAmount>
                <DuePayableAmount>119.00</DuePayableAmount>
            </SpecifiedTradeSettlementHeaderMonetarySummation>
        </ApplicableHeaderTradeSettlement>
    </ns3:SupplyChainTradeTransaction>
</ns3:CrossIndustryInvoice>
```

</details>

<details><summary>Raw response Data:</summary>

```xml
</rep:report>➜  diga-api-client git:(add_documentation_for_verification_api) ✗  cd /Users/srehfeldt/Desktop/repositories/diga-api-client ; /usr/bin/env /Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home/bin/java -agentlib:jdwp=transport=dt_socket,server=n,suspend=y,address=localhost:50207 -Dfile.encoding=UTF-8 @/var/folders/_4/lksyvn0153z0m0hsy9mbc_540000gn/T/cp_55eo6njelbopyxv5n2td61jnl.argfile com.alextherapeutics.diga.Main 
12:37:29.872 [main] DEBUG nl.altindag.ssl.trustmanager.CompositeX509ExtendedTrustManager - Received the following server certificate: [CN=*.bitmarck-daten.de]
12:37:39.590 [main] DEBUG nl.altindag.ssl.trustmanager.CompositeX509ExtendedTrustManager - Received the following server certificate: [CN=*.bitmarck-daten.de]<rep:report xmlns:html="http://www.w3.org/1999/xhtml" xmlns:in="http://www.xoev.de/de/validator/framework/1/createreportinput" xmlns:rep="http://www.xoev.de/de/validator/varl/1" xmlns:s="http://www.xoev.de/de/validator/framework/1/scenarios" xmlns:svrl="http://purl.oclc.org/dsdl/svrl" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" varlVersion="1.0.0" valid="false">
    <rep:engine>
        <rep:name>KoSIT Validator 1.4.2</rep:name>
    </rep:engine>
    <rep:timestamp>2021-07-23T12:37:39.819+02:00</rep:timestamp>
    <rep:documentIdentification>
        <rep:documentHash>
            <rep:hashAlgorithm>SHA-256</rep:hashAlgorithm>
            <rep:hashValue>xITwAEQ7VL/JYYKbFNdgMfHlDI+Gn8uJNJTviN6TWWU=</rep:hashValue>
        </rep:documentHash>
        <rep:documentReference>StreamSource</rep:documentReference>
    </rep:documentIdentification>
    <rep:scenarioMatched>
        <s:scenario>
            <s:name>DiGA-Rechnung (DRE0-Anfrage) basierend auf EN16931 CIUS XRechnung (UN/CEFACT CII 100.D16B)</s:name>
            <s:description>
                <s:p>Voraussetzung für diese Prüfung ist, dass es sich um eine gültige XRechnung gemäß Version 1.2 oder 2.0                 handelt. Bitte verwenden Sie dafür im ersten Schritt die "validator-configuration-xrechnung" in der                 entsprechenden Version, bevor Sie diese Prüfung durchführen.</s:p>
            </s:description>
            <s:namespace prefix="ram">urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100</s:namespace>
            <s:namespace prefix="rep">http://www.xoev.de/de/validator/varl/1</s:namespace>
            <s:namespace prefix="rsm">urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100</s:namespace>
            <s:match>/rsm:CrossIndustryInvoice[starts-with(normalize-space(rsm:ExchangedDocumentContext/ram:GuidelineSpecifiedDocumentContextParameter/ram:ID),
            'urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_')]</s:match>
            <s:validateWithXmlSchema>
                <s:resource>
                    <s:name>XML-Schema-Dummy für das Abrechnen von Freischaltcodes (basierend auf CII)</s:name>
                    <s:location>xml-schema/dre0/dummy-cii.xsd</s:location>
                </s:resource>
            </s:validateWithXmlSchema>
            <s:validateWithSchematron>
                <s:resource>
                    <s:name>Schematron-Regeln für das Abrechnen von Freischaltcodes</s:name>
                    <s:location>schematron/dre0.xsl</s:location>
                </s:resource>
            </s:validateWithSchematron>
            <s:createReport>
                <s:resource>
                    <s:name>Report für das Abrechnen von Freischaltcodes</s:name>
                    <s:location>report/dre0.xsl</s:location>
                </s:resource>
            </s:createReport>
            <s:acceptMatch>/rep:report/rep:assessment[1]/rep:accept[1]</s:acceptMatch>
        </s:scenario>
        <rep:documentData xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100" xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100" xmlns:ubl="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100">
            <seller>manufacturingCompanyName</seller>
            <id>1</id>
            <issueDate>20210723</issueDate>
        </rep:documentData>
        <rep:validationStepResult id="val-xsd" valid="true">
            <s:resource>
                <s:name>XML-Schema-Dummy für das Abrechnen von Freischaltcodes (basierend auf CII)</s:name>
                <s:location>xml-schema/dre0/dummy-cii.xsd</s:location>
            </s:resource>
        </rep:validationStepResult>
        <rep:validationStepResult id="val-sch.1" valid="true">
            <s:resource>
                <s:name>Schematron-Regeln für das Abrechnen von Freischaltcodes</s:name>
                <s:location>schematron/dre0.xsl</s:location>
            </s:resource>
        </rep:validationStepResult>
        <rep:validationStepResult id="INVER" valid="false">
            <s:resource>
                <s:name>Rechnungsprüfung</s:name>
                <s:location>???</s:location>
            </s:resource>
            <rep:message xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100" id="INVER-1" code="INVER-1" level="error" xpathLocation="rsm:CrossIndustryInvoice">Freischaltcode / Rezeptcode nicht gefunden: 77AA AAAA AAAA AAAX</rep:message>
        </rep:validationStepResult>
    </rep:scenarioMatched>
    <rep:assessment>
        <rep:reject>
            <rep:explanation>
                <html xmlns="http://www.w3.org/1999/xhtml" data-report-type="report">
                    <head>
                        <title>Prüfbericht</title>
                        <style>
                body{
                font-family: Calibri;
                width: 230mm;
                }
                
                .metadata dt {
                float: left;
                width: 230px;
                clear: left;
                }
                
                .metadata dd {
                margin-left: 250px;
                }
                
                table{
                border-collapse: collapse;
                width: 100%;
                }
                
                table.tbl-errors{
                font-size: smaller;
                }
               
                table.document{
                font-size: smaller;
                }
               
                table.document td {vertical-align:top;}
                
                .tbl-errors td{
                border: 1px solid lightgray;
                padding: 2px;
                vertical-align: top;
                }
                
                thead{
                font-weight: bold;
                background-color: #f0f0f0;
                padding-top: 6pt;
                padding-bottom: 2pt;
                }
                
                .tbl-meta td{
                padding-right: 1em;
                }
                
                td.pos{
                padding-left: 3pt;
                width: 5%;
                color: gray
                }
                
                td.element{
                width: 95%;
                word-wrap: break-word;
                }
                
                
                td.element:before{
                content: attr(title);
                color: gray;
                }
                
                
                div.attribute{
                display: inline;
                font-style: italic;
                color: gray;
                }
                div.attribute:before{
                content: attr(title) '=';
                }
                div.val{
                display: inline;
                font-weight: bold;
                }
                
                td.level1{
                padding-left: 2mm;
                }
                
                td.level2{
                padding-left: 5mm;
                }
                
                td.level3{
                padding-left: 10mm;
                }
                
                td.level4{
                padding-left: 15mm;
                }
                
                td.level5{
                padding-left: 20mm;
                }
                td.level6{
                padding-left: 25mm;
                }
                
                tr{
                vertical-align: bottom;
                border-bottom: 1px solid #c0c0c0;
                }
                
                .error{
                color: red;
                }
                
                .warning{
                }
                
                p.important{
                font-weight: bold;
                text-align: left;
                background-color: #e0e0e0;
                padding: 3pt;
                }
                
                td.right{
                text-align: right
                }</style>
                    </head>
                    <body>
                        <h1>Prüfbericht</h1>
                        <div class="metadata">
                            <p class="important">Angaben zum geprüften Dokument</p>
                            <dl>
                                <dt>Referenz:</dt>
                                <dd>StreamSource</dd>
                                <dt>Zeitpunkt der Prüfung:</dt>
                                <dd>23.7.2021 12:37:39</dd>
                                <dt>Erkannter Dokumenttyp:</dt>
                                <dd>DiGA-Rechnung (DRE0-Anfrage) basierend auf EN16931 CIUS XRechnung (UN/CEFACT CII 100.D16B)</dd>
                            </dl>
                            <dl xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100" xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100" xmlns:ubl="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2" xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100">
                                <dt>Erkannter Rechnungssteller:</dt>
                                <dd>manufacturingCompanyName</dd>
                                <dt>Erkannte Rechnungsnummer:</dt>
                                <dd>1</dd>
                                <dt>Erkanntes Rechnungsdatum:</dt>
                                <dd>20210723</dd>
                            </dl>
                        </div>
                        <p class="important">
                            <b>Konformitätsprüfung: </b>
                            Das geprüfte Dokument enthält weder Fehler noch Warnungen. Es ist konform zu den formalen Vorgaben.
                        </p>
                        <p class="important">Bewertung: Es wird empfohlen das Dokument anzunehmen und weiter zu verarbeiten.</p>
                        <p class="important">Inhalt des Dokuments:</p>
                        <table class="document">
                            <tr class="row" id="0001">
                                <td class="pos">0001</td>
                                <td class="element level1" title="CrossIndustryInvoice">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0002">
                                <td class="pos">0002</td>
                                <td class="element level2" title="ExchangedDocumentContext">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0003">
                                <td class="pos">0003</td>
                                <td class="element level3" title="GuidelineSpecifiedDocumentContextParameter">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0004">
                                <td class="pos">0004</td>
                                <td class="element level4" title="ID">
                                    <div class="val">urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0</div>
                                </td>
                            </tr>
                            <tr class="row" id="0005">
                                <td class="pos">0005</td>
                                <td class="element level2" title="ExchangedDocument">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0006">
                                <td class="pos">0006</td>
                                <td class="element level3" title="ID">
                                    <div class="val">1</div>
                                </td>
                            </tr>
                            <tr class="row" id="0007">
                                <td class="pos">0007</td>
                                <td class="element level3" title="TypeCode">
                                    <div class="val">380</div>
                                </td>
                            </tr>
                            <tr class="row" id="0008">
                                <td class="pos">0008</td>
                                <td class="element level3" title="IssueDateTime">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0009">
                                <td class="pos">0009</td>
                                <td class="element level4" title="DateTimeString">
                                    <div class="val">20210723</div>
                                    <div class="attribute" title="format">102</div>
                                </td>
                            </tr>
                            <tr class="row" id="0010">
                                <td class="pos">0010</td>
                                <td class="element level2" title="SupplyChainTradeTransaction">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0011">
                                <td class="pos">0011</td>
                                <td class="element level3" title="IncludedSupplyChainTradeLineItem">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0012">
                                <td class="pos">0012</td>
                                <td class="element level4" title="AssociatedDocumentLineDocument">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0013">
                                <td class="pos">0013</td>
                                <td class="element level5" title="LineID">
                                    <div class="val">1</div>
                                </td>
                            </tr>
                            <tr class="row" id="0014">
                                <td class="pos">0014</td>
                                <td class="element level4" title="SpecifiedTradeProduct">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0015">
                                <td class="pos">0015</td>
                                <td class="element level5" title="GlobalID">
                                    <div class="val">12345000</div>
                                    <div class="attribute" title="schemeID">DiGAVEID</div>
                                </td>
                            </tr>
                            <tr class="row" id="0016">
                                <td class="pos">0016</td>
                                <td class="element level5" title="BuyerAssignedID">
                                    <div class="val">77AAAAAAAAAAAAAX</div>
                                    <div class="attribute" title="schemeID">Freischaltcode</div>
                                </td>
                            </tr>
                            <tr class="row" id="0017">
                                <td class="pos">0017</td>
                                <td class="element level5" title="Name">
                                    <div class="val">digaName</div>
                                </td>
                            </tr>
                            <tr class="row" id="0018">
                                <td class="pos">0018</td>
                                <td class="element level5" title="Description">
                                    <div class="val">A digaName prescription.</div>
                                </td>
                            </tr>
                            <tr class="row" id="0019">
                                <td class="pos">0019</td>
                                <td class="element level4" title="SpecifiedLineTradeAgreement">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0020">
                                <td class="pos">0020</td>
                                <td class="element level5" title="NetPriceProductTradePrice">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0021">
                                <td class="pos">0021</td>
                                <td class="element level6" title="ChargeAmount">
                                    <div class="val">100</div>
                                </td>
                            </tr>
                            <tr class="row" id="0022">
                                <td class="pos">0022</td>
                                <td class="element level4" title="SpecifiedLineTradeDelivery">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0023">
                                <td class="pos">0023</td>
                                <td class="element level5" title="BilledQuantity">
                                    <div class="val">1</div>
                                    <div class="attribute" title="unitCode">C62</div>
                                </td>
                            </tr>
                            <tr class="row" id="0024">
                                <td class="pos">0024</td>
                                <td class="element level4" title="SpecifiedLineTradeSettlement">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0025">
                                <td class="pos">0025</td>
                                <td class="element level5" title="ApplicableTradeTax">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0026">
                                <td class="pos">0026</td>
                                <td class="element level6" title="TypeCode">
                                    <div class="val">VAT</div>
                                </td>
                            </tr>
                            <tr class="row" id="0027">
                                <td class="pos">0027</td>
                                <td class="element level6" title="CategoryCode">
                                    <div class="val">S</div>
                                </td>
                            </tr>
                            <tr class="row" id="0028">
                                <td class="pos">0028</td>
                                <td class="element level6" title="RateApplicablePercent">
                                    <div class="val">19</div>
                                </td>
                            </tr>
                            <tr class="row" id="0029">
                                <td class="pos">0029</td>
                                <td class="element level5" title="SpecifiedTradeSettlementLineMonetarySummation">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0030">
                                <td class="pos">0030</td>
                                <td class="element level6" title="LineTotalAmount">
                                    <div class="val">100</div>
                                </td>
                            </tr>
                            <tr class="row" id="0031">
                                <td class="pos">0031</td>
                                <td class="element level3" title="ApplicableHeaderTradeAgreement">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0032">
                                <td class="pos">0032</td>
                                <td class="element level4" title="BuyerReference">
                                    <div class="val">Leitweg-ID</div>
                                </td>
                            </tr>
                            <tr class="row" id="0033">
                                <td class="pos">0033</td>
                                <td class="element level4" title="SellerTradeParty">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0034">
                                <td class="pos">0034</td>
                                <td class="element level5" title="ID">
                                    <div class="val">IK000000000</div>
                                </td>
                            </tr>
                            <tr class="row" id="0035">
                                <td class="pos">0035</td>
                                <td class="element level5" title="ID">
                                    <div class="val">000000000</div>
                                    <div class="attribute" title="schemeID">IK</div>
                                </td>
                            </tr>
                            <tr class="row" id="0036">
                                <td class="pos">0036</td>
                                <td class="element level5" title="Name">
                                    <div class="val">manufacturingCompanyName</div>
                                </td>
                            </tr>
                            <tr class="row" id="0037">
                                <td class="pos">0037</td>
                                <td class="element level5" title="DefinedTradeContact">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0038">
                                <td class="pos">0038</td>
                                <td class="element level6" title="PersonName">
                                    <div class="val">fullName</div>
                                </td>
                            </tr>
                            <tr class="row" id="0039">
                                <td class="pos">0039</td>
                                <td class="element level6" title="TelephoneUniversalCommunication">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0040">
                                <td class="pos">0040</td>
                                <td class="element level7" title="CompleteNumber">
                                    <div class="val">phoneNumber</div>
                                </td>
                            </tr>
                            <tr class="row" id="0041">
                                <td class="pos">0041</td>
                                <td class="element level6" title="EmailURIUniversalCommunication">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0042">
                                <td class="pos">0042</td>
                                <td class="element level7" title="URIID">
                                    <div class="val">svensvensson@awesomedigacompany.com</div>
                                </td>
                            </tr>
                            <tr class="row" id="0043">
                                <td class="pos">0043</td>
                                <td class="element level5" title="PostalTradeAddress">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0044">
                                <td class="pos">0044</td>
                                <td class="element level6" title="PostcodeCode">
                                    <div class="val">postalCode</div>
                                </td>
                            </tr>
                            <tr class="row" id="0045">
                                <td class="pos">0045</td>
                                <td class="element level6" title="LineOne">
                                    <div class="val">adressLine</div>
                                </td>
                            </tr>
                            <tr class="row" id="0046">
                                <td class="pos">0046</td>
                                <td class="element level6" title="CityName">
                                    <div class="val">city</div>
                                </td>
                            </tr>
                            <tr class="row" id="0047">
                                <td class="pos">0047</td>
                                <td class="element level6" title="CountryID">
                                    <div class="val">DE</div>
                                </td>
                            </tr>
                            <tr class="row" id="0048">
                                <td class="pos">0048</td>
                                <td class="element level5" title="SpecifiedTaxRegistration">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0049">
                                <td class="pos">0049</td>
                                <td class="element level6" title="ID">
                                    <div class="val">DE 123 456 789</div>
                                    <div class="attribute" title="schemeID">VA</div>
                                </td>
                            </tr>
                            <tr class="row" id="0050">
                                <td class="pos">0050</td>
                                <td class="element level4" title="BuyerTradeParty">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0051">
                                <td class="pos">0051</td>
                                <td class="element level5" title="ID">
                                    <div class="val">IK109034270</div>
                                </td>
                            </tr>
                            <tr class="row" id="0052">
                                <td class="pos">0052</td>
                                <td class="element level5" title="ID">
                                    <div class="val">109034270</div>
                                    <div class="attribute" title="schemeID">IK</div>
                                </td>
                            </tr>
                            <tr class="row" id="0053">
                                <td class="pos">0053</td>
                                <td class="element level5" title="Name">
                                    <div class="val">BMW BKK   </div>
                                </td>
                            </tr>
                            <tr class="row" id="0054">
                                <td class="pos">0054</td>
                                <td class="element level5" title="PostalTradeAddress">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0055">
                                <td class="pos">0055</td>
                                <td class="element level6" title="PostcodeCode">
                                    <div class="val">84130</div>
                                </td>
                            </tr>
                            <tr class="row" id="0056">
                                <td class="pos">0056</td>
                                <td class="element level6" title="LineOne">
                                    <div class="val">Mengkofener Str. 6</div>
                                </td>
                            </tr>
                            <tr class="row" id="0057">
                                <td class="pos">0057</td>
                                <td class="element level6" title="CityName">
                                    <div class="val">Dingolfing</div>
                                </td>
                            </tr>
                            <tr class="row" id="0058">
                                <td class="pos">0058</td>
                                <td class="element level6" title="CountryID">
                                    <div class="val">DE</div>
                                </td>
                            </tr>
                            <tr class="row" id="0059">
                                <td class="pos">0059</td>
                                <td class="element level3" title="ApplicableHeaderTradeDelivery">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0060">
                                <td class="pos">0060</td>
                                <td class="element level4" title="ActualDeliverySupplyChainEvent">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0061">
                                <td class="pos">0061</td>
                                <td class="element level5" title="OccurrenceDateTime">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0062">
                                <td class="pos">0062</td>
                                <td class="element level6" title="DateTimeString">
                                    <div class="val">20210723</div>
                                    <div class="attribute" title="format">102</div>
                                </td>
                            </tr>
                            <tr class="row" id="0063">
                                <td class="pos">0063</td>
                                <td class="element level3" title="ApplicableHeaderTradeSettlement">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0064">
                                <td class="pos">0064</td>
                                <td class="element level4" title="CreditorReferenceID">
                                    <div class="val">000000000</div>
                                    <div class="attribute" title="schemeID">IK</div>
                                </td>
                            </tr>
                            <tr class="row" id="0065">
                                <td class="pos">0065</td>
                                <td class="element level4" title="InvoiceCurrencyCode">
                                    <div class="val">EUR</div>
                                </td>
                            </tr>
                            <tr class="row" id="0066">
                                <td class="pos">0066</td>
                                <td class="element level4" title="SpecifiedTradeSettlementPaymentMeans">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0067">
                                <td class="pos">0067</td>
                                <td class="element level5" title="TypeCode">
                                    <div class="val">30</div>
                                </td>
                            </tr>
                            <tr class="row" id="0068">
                                <td class="pos">0068</td>
                                <td class="element level4" title="ApplicableTradeTax">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0069">
                                <td class="pos">0069</td>
                                <td class="element level5" title="CalculatedAmount">
                                    <div class="val">19.00</div>
                                </td>
                            </tr>
                            <tr class="row" id="0070">
                                <td class="pos">0070</td>
                                <td class="element level5" title="TypeCode">
                                    <div class="val">VAT</div>
                                </td>
                            </tr>
                            <tr class="row" id="0071">
                                <td class="pos">0071</td>
                                <td class="element level5" title="BasisAmount">
                                    <div class="val">100.00</div>
                                </td>
                            </tr>
                            <tr class="row" id="0072">
                                <td class="pos">0072</td>
                                <td class="element level5" title="CategoryCode">
                                    <div class="val">S</div>
                                </td>
                            </tr>
                            <tr class="row" id="0073">
                                <td class="pos">0073</td>
                                <td class="element level5" title="RateApplicablePercent">
                                    <div class="val">19</div>
                                </td>
                            </tr>
                            <tr class="row" id="0074">
                                <td class="pos">0074</td>
                                <td class="element level4" title="SpecifiedTradePaymentTerms">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0075">
                                <td class="pos">0075</td>
                                <td class="element level5" title="Description" />
                            </tr>
                            <tr class="row" id="0076">
                                <td class="pos">0076</td>
                                <td class="element level4" title="SpecifiedTradeSettlementHeaderMonetarySummation">
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                    <div class="val"></div>
                                </td>
                            </tr>
                            <tr class="row" id="0077">
                                <td class="pos">0077</td>
                                <td class="element level5" title="LineTotalAmount">
                                    <div class="val">100.00</div>
                                </td>
                            </tr>
                            <tr class="row" id="0078">
                                <td class="pos">0078</td>
                                <td class="element level5" title="TaxBasisTotalAmount">
                                    <div class="val">100.00</div>
                                </td>
                            </tr>
                            <tr class="row" id="0079">
                                <td class="pos">0079</td>
                                <td class="element level5" title="TaxTotalAmount">
                                    <div class="val">19.00</div>
                                    <div class="attribute" title="currencyID">EUR</div>
                                </td>
                            </tr>
                            <tr class="row" id="0080">
                                <td class="pos">0080</td>
                                <td class="element level5" title="GrandTotalAmount">
                                    <div class="val">119.00</div>
                                </td>
                            </tr>
                            <tr class="row" id="0081">
                                <td class="pos">0081</td>
                                <td class="element level5" title="DuePayableAmount">
                                    <div class="val">119.00</div>
                                </td>
                            </tr>
                        </table>
                        <p class="info">Dieser Prüfbericht wurde erstellt mit KoSIT Validator 1.4.2.</p>
                    </body>
                </html>
            </rep:explanation>
        </rep:reject>
    </rep:assessment>
</rep:report>
```

</details>

<details><summary>Generated invoice by the diga-api-client:</summary>

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns3:CrossIndustryInvoice xmlns="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100" xmlns:ns2="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100" xmlns:ns3="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100" xmlns:ns4="urn:un:unece:uncefact:data:standard:QualifiedDataType:100">
    <ns3:ExchangedDocumentContext>
        <GuidelineSpecifiedDocumentContextParameter>
            <ID>urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0</ID>
        </GuidelineSpecifiedDocumentContextParameter>
    </ns3:ExchangedDocumentContext>
    <ns3:ExchangedDocument>
        <ID>1</ID>
        <TypeCode>380</TypeCode>
        <IssueDateTime>
            <ns2:DateTimeString format="102">20210723</ns2:DateTimeString>
        </IssueDateTime>
    </ns3:ExchangedDocument>
    <ns3:SupplyChainTradeTransaction>
        <IncludedSupplyChainTradeLineItem>
            <AssociatedDocumentLineDocument>
                <LineID>1</LineID>
            </AssociatedDocumentLineDocument>
            <SpecifiedTradeProduct>
                <GlobalID schemeID="DiGAVEID">12345000</GlobalID>
                <BuyerAssignedID schemeID="Freischaltcode">77AAAAAAAAAAAAAX</BuyerAssignedID>
                <Name>digaName</Name>
                <Description>A digaName prescription.</Description>
            </SpecifiedTradeProduct>
            <SpecifiedLineTradeAgreement>
                <NetPriceProductTradePrice>
                    <ChargeAmount>100</ChargeAmount>
                </NetPriceProductTradePrice>
            </SpecifiedLineTradeAgreement>
            <SpecifiedLineTradeDelivery>
                <BilledQuantity unitCode="C62">1</BilledQuantity>
            </SpecifiedLineTradeDelivery>
            <SpecifiedLineTradeSettlement>
                <ApplicableTradeTax>
                    <TypeCode>VAT</TypeCode>
                    <CategoryCode>S</CategoryCode>
                    <RateApplicablePercent>19</RateApplicablePercent>
                </ApplicableTradeTax>
                <SpecifiedTradeSettlementLineMonetarySummation>
                    <LineTotalAmount>100</LineTotalAmount>
                </SpecifiedTradeSettlementLineMonetarySummation>
            </SpecifiedLineTradeSettlement>
        </IncludedSupplyChainTradeLineItem>
        <ApplicableHeaderTradeAgreement>
            <BuyerReference>Leitweg-ID</BuyerReference>
            <SellerTradeParty>
                <ID>IK000000000</ID>
                <ID schemeID="IK">000000000</ID>
                <Name>manufacturingCompanyName</Name>
                <DefinedTradeContact>
                    <PersonName>fullName</PersonName>
                    <TelephoneUniversalCommunication>
                        <CompleteNumber>phoneNumber</CompleteNumber>
                    </TelephoneUniversalCommunication>
                    <EmailURIUniversalCommunication>
                        <URIID>svensvensson@awesomedigacompany.com</URIID>
                    </EmailURIUniversalCommunication>
                </DefinedTradeContact>
                <PostalTradeAddress>
                    <PostcodeCode>postalCode</PostcodeCode>
                    <LineOne>adressLine</LineOne>
                    <CityName>city</CityName>
                    <CountryID>DE</CountryID>
                </PostalTradeAddress>
                <SpecifiedTaxRegistration>
                    <ID schemeID="VA">DE 123 456 789</ID>
                </SpecifiedTaxRegistration>
            </SellerTradeParty>
            <BuyerTradeParty>
                <ID>IK111111111</ID>
                <ID schemeID="IK">111111111</ID>
                <Name>AOK NordWest - Die Gesundheitskasse</Name>
                <PostalTradeAddress>
                    <PostcodeCode>44269</PostcodeCode>
                    <LineOne>Kopenhagener Str. 1</LineOne>
                    <CityName>Dortmund</CityName>
                    <CountryID>DE</CountryID>
                </PostalTradeAddress>
            </BuyerTradeParty>
        </ApplicableHeaderTradeAgreement>
        <ApplicableHeaderTradeDelivery>
            <ActualDeliverySupplyChainEvent>
                <OccurrenceDateTime>
                    <ns2:DateTimeString format="102">20210723</ns2:DateTimeString>
                </OccurrenceDateTime>
            </ActualDeliverySupplyChainEvent>
        </ApplicableHeaderTradeDelivery>
        <ApplicableHeaderTradeSettlement>
            <CreditorReferenceID schemeID="IK">000000000</CreditorReferenceID>
            <InvoiceCurrencyCode>EUR</InvoiceCurrencyCode>
            <SpecifiedTradeSettlementPaymentMeans>
                <TypeCode>30</TypeCode>
            </SpecifiedTradeSettlementPaymentMeans>
            <ApplicableTradeTax>
                <CalculatedAmount>19.00</CalculatedAmount>
                <TypeCode>VAT</TypeCode>
                <BasisAmount>100.00</BasisAmount>
                <CategoryCode>S</CategoryCode>
                <RateApplicablePercent>19</RateApplicablePercent>
            </ApplicableTradeTax>
            <SpecifiedTradePaymentTerms>
                <Description></Description>
            </SpecifiedTradePaymentTerms>
            <SpecifiedTradeSettlementHeaderMonetarySummation>
                <LineTotalAmount>100.00</LineTotalAmount>
                <TaxBasisTotalAmount>100.00</TaxBasisTotalAmount>
                <TaxTotalAmount currencyID="EUR">19.00</TaxTotalAmount>
                <GrandTotalAmount>119.00</GrandTotalAmount>
                <DuePayableAmount>119.00</DuePayableAmount>
            </SpecifiedTradeSettlementHeaderMonetarySummation>
        </ApplicableHeaderTradeSettlement>
    </ns3:SupplyChainTradeTransaction>
</ns3:CrossIndustryInvoice>
```

</details>

## Encryption

Given that we are dealing with patient data, it is required to properly encrypt the data sent within requests.
The encryption is based on certificates which need to be requested at the ITSG which acts as a trust center.
You can request it at [ITSG website](https://www.itsg.de/produkte/trust-center/zertifikat-beantragen/), assuming that you already have an IK number.
It takes approximately a week to received the certificates back.

A technical documentation for the encryption (of course in German) can be found on the [technical standards page at gkv](https://www.gkv-datenaustausch.de/technische_standards_1/technische_standards.jsp) as [attachment 16](https://www.gkv-datenaustausch.de/media/dokumente/standards_und_normen/technische_spezifikationen/Anlage_16.pdf) under `Security Schnittstelle (SECON)` section.

A [short guide](https://www.itsg.de/wp-content/uploads/2020/11/Trust-Center-howto_p10_openssl_rsa4096.pdf) (in German) for how to generate the public/private key pair with `OpenSSL` is available on the ITSG website under the [FAQ: _Was versteht man unter einem "Hashcode oder Fingerprint des öffentlichen Schlüssels"?_](https://www.itsg.de/produkte/trust-center/fragen-und-antworten-faq/).
It also includes the command for how to export the digest/fingerprint of the public key which must be included in the application to ITSG.

Once the certificates are issued, checkout the [prerequisites from the Readme](https://github.com/alex-therapeutics/diga-api-client#prerequisites) to be able to use them with the api client.

### Useful Links

Updated on: 2021-07-02

- Main page for application process: https://www.itsg.de/produkte/trust-center/zertifikat-beantragen/
- FAQ https://www.itsg.de/produkte/trust-center/fragen-und-antworten-faq/
- How to generate key for certificate: https://www.itsg.de/wp-content/uploads/2020/11/Trust-Center-howto_p10_openssl_rsa4096.pdf
- How to get the certificate: https://www.itsg.de/wp-content/uploads/2020/11/Trust-Center-141016_So_erhalten_Sie_Ihr_Zertifikat_als_Leistungserbringer.pdf
- Application Form: https://www.itsg.de/wp-content/uploads/2020/11/Trust-Center-Muster-Zertifizierungsantrag-200203.pdf
  - Example form: https://www.itsg.de/wp-content/uploads/2020/11/Trust-Center-Ausfuellhilfe-141016.pdf
- Online tracking of application process after submission: https://www.itsg-trust.de/all/antrag.php

## Auditing

The communication with the DiGA api must be logged by the DiGA manufacturer.
Details are described in [section 4.4 - Protokollierung](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_Anlage_1_Technische_Anlage_zur_RL_V1.1_20210225.pdf).

```
Die Protokollierung muss die folgenden Mindestinhalte umfassen:
- Eindeutige Bezeichnung der Kommunikationspartner (Institutionskennzeichen)
- Zeitstempel im Format „yyyy-MM-dd HH:mm:ss“, basierend auf deutscher Zeit
- Prozesskennzeichen (Prüfung Freischaltcode / Rezeptcode / Abrechnung)
- Freischaltcode / Rezeptcode
- Verarbeitungskennzeichen (fehlerfrei/fehlerhaft)
```

Translated fields:

- ik numbers
- timestamp in German timezone
- value for `verfahren`
- prescription code
- flag if code os valid or not

# FAQ

## What can I do if I want to make requests but my DiGA is not approved yet and I therefore do not have a diga id?

Just sent some 5 digit number as part of the `nutzdaten`, e.g. `12345`.

## What can I do if I dont know the IK number yet and still want to test the client?

You will need an IK number to request the certificate for encryption and decryption. Therefore, it is a must-have.

## Are there solutions for non-java users?

You can find an api-wrapper, developed in Kotlin, [here](https://github.com/gtuk/diga-api-service).
It also comes with a Dockerfile to facilitate integration into your system.

## My DiGA (app) is only valid for 30 days. Can users request a fresh code from their insurer while their existing code is still valid?

**TODO** Hopefully, users want to continue using your DiGA after their initial prescription code expired.

- Can they already request a new code while still being active?
- How will the verification and billing api handle these cases?
- Is the `TagDerLeistungserbringung` the date on which the request is made or is this set by the insurer when issuing the prescription code?

## What are expected response times for the requests?

The official documentation mentions SLOs of 5.6sec, 98% from Monday-Friday 8-20 and 90% outside.
According to some requests from running in production the response times range from ~1-8 seconds.
The times differ significantly between insurances (bitmark api is fastest and used by most insurances) but also between code validation and billing requests.
Billing usually takes ~1-4 seconds longer.

## How can we handle error responses with codes 201 or 202?

The status codes refer to `Server Error` and `Memory Error` and are expected if e.g. the connection drops.
In general it is no problem to retry validation and billing requests in those cases.

# Glossary

| Term (German original)  | Short description in English  |
| ----------------------- | ----------------------------- |
| Institutionskennzeichen | Unique of a DiGA manufacturer |

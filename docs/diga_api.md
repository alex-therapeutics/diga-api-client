# DiGA API documentation

This document aims to help DiGA manufacturers figure out how prescription codes can be verified and used for billing.

- [DiGA API documentation](#diga-api-documentation)
- [Overview](#overview)
- [Summary](#summary)
- [The prescription code](#the-prescription-code)
  - [Mapping file for insurance companies](#mapping-file-for-insurance-companies)
- [Request format](#request-format)
  - [Institutionskennzeichen - IK numbers](#institutionskennzeichen---ik-numbers)
    - [Requesting an IK number](#requesting-an-ik-number)
  - [Verfahren](#verfahren)
  - [Nutzdaten](#nutzdaten)
- [Request details](#request-details)
  - [Code verification requests](#code-verification-requests)
  - [Billing requests](#billing-requests)
  - [Encryption](#encryption)
- [FAQ](#faq)
  - [What can I do if I want to make requests but my DiGA is not approved yet and I therefore do not have a diga id?](#what-can-i-do-if-i-want-to-make-requests-but-my-diga-is-not-approved-yet-and-i-therefore-do-not-have-a-diga-id)
  - [What can I do if I dont know the IK number yet and still want to test the client?](#what-can-i-do-if-i-dont-know-the-ik-number-yet-and-still-want-to-test-the-client)
  - [Are there solutions for non-java users?](#are-there-solutions-for-non-java-users)
  - [My DiGA (app) is only valid for 30 days. Can users request a fresh code from their insurer while their existing code is still valid?](#my-diga-app-is-only-valid-for-30-days-can-users-request-a-fresh-code-from-their-insurer-while-their-existing-code-is-still-valid)
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
| 102          | 77AAAAAAAAAAADGE | Code not found          | E.g. code is not linked to DiGA                                                                                                                          |
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
- 2 = email
- 3 = post

Although insurance companies use their own apis, they do follow an openapi specification for the request which is listed as [DiGA-YAML-Datei (YAML)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/digaSP_1_0_05.yaml) at the same [gkv page](https://www.gkv-datenaustausch.de/leistungserbringer/digitale_gesundheitsanwendungen/digitale_gesundheitsanwendungen.jsp).
This [documents](https://github.com/alex-therapeutics/diga-api-client/blob/main/ENDPOINT_STATUS.md) lists which apis are currently working with the client.

# Request format

According to the openapi specification the request contains 4 different parameters which are sent with `ContentType: multipart/form-data`:

- `iksender` - Institutionskennzeichen (IK) of the DiGA manufacturer (an id required for payments)
- `ikempfaenger` - IK of the insurer which can be found in the mapping file (`Kostentraegerkennung` - [section 5.2.2](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_Anlage_1_Technische_Anlage_zur_RL_V1.1_20210225.pdf))
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
The form can be sent by email and a blueprint can be downloaded [here](https://www.dguv.de/medien/arge-ik/downloads/erfass.pdf).

Additional information at [gkv page](https://www.gkv-datenaustausch.de/leistungserbringer/sonstige_leistungserbringer/sonstige_leistungserbringer.jsp) can be found in [20210401_Broschuere_TP5 (PDF)](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/sonstige_leistungserbringer/20210401_Broschuere_TP5.pdf)
which also contains a section about requesting certificates from ITSG trust center.

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

Data for sending the request:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Pruefung_Freischaltcode xmlns="http://www.gkv-datenaustausch.de/XML-Schema/EDFC0_Pruefung/2.0.0"
                         version="002.000.000" gueltigab="2020-07-01" verfahrenskennung="TDFC0" nachrichtentyp="ANF"
                         absender="987654321" empfaenger="123456789">
    <Antwort>
        <IK_DiGA_Hersteller>123456789</IK_DiGA_Hersteller>
        <IK_Krankenkasse>987654321</IK_Krankenkasse>
        <DiGAID>12345000</DiGAVEID>
        <Freischaltcode>ABCDEFGHIJKLMNOP</Freischaltcode>
    </Antwort>
</Pruefung_Freischaltcode>
```

In case you are not an approved DiGA manufacturer yet, you can put `12345` as `DiGAID`.
You should already have an `IK` number though as otherwise, you also wouldn't have the certificate for encryption/decryption.

Successful response data:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Pruefung_Freischaltcode xmlns="http://www.gkv-datenaustausch.de/XML-Schema/EDFC0_Pruefung/2.0.0"
                         version="002.000.000" gueltigab="2020-07-01" verfahrenskennung="TDFC0" nachrichtentyp="ANT"
                         absender="987654321" empfaenger="123456789">
    <Antwort>
        <IK_DiGA_Hersteller>123456789</IK_DiGA_Hersteller>
        <IK_Krankenkasse>987654321</IK_Krankenkasse>
        <DiGAVEID>12345000</DiGAVEID>
        <Freischaltcode>ABCDEFGHIJKLMNOP</Freischaltcode>
        <Tag_der_Leistungserbringung>2020-08-19</Tag_der_Leistungserbringung>
    </Antwort>
</Pruefung_Freischaltcode>
```

The response differs from the request by having `nachrichtentyp="ANT"` as header (`Antwort`) instead of `nachrichtentyp="ANF"` (`Anfrage`).
Also the diga id is listed as `DiGAVEID` (verified ID) and an additional field `Tag_der_Leistungserbringung` is returned (day when the service started).

## Billing requests

Invoices must be created using the `XRechnung` standard.
There might also be dedicated accounting/book-keeping software which allows creating and sending invoices according to this standard, so that billing might also be handled outside the production system.
However, the client supports sending invoices by mail.
It is up to the users of the client to define what should happen with the response from the diga api.
E.g. invoices are likely to be required for accounting and therefore must be stored.
Also handling of apis which do not support billing yet, must be defined.

The invoice should contain the following information:

- prescription code
- DiGA id of the manufacturer
- time period for prescription
- information about the invoice issuer and the insurer

Additional information can be found on this [wiki page](https://github.com/alex-therapeutics/diga-api-client/wiki/Billing-validation---XRechnung).

## Encryption

Given that we are dealing with patient data, it is required to properly encrypt the data sent within requests.
The encryption is based on certificates which need to be requested at the ITSG which acts as a trust center.
You can request it at [ITSG website](https://www.itsg.de/produkte/trust-center/zertifikat-beantragen/), assuming that you already have an IK number.

A technical documentation for the encryption (of course in German) can be found on the [technical standards page at gkv](https://www.gkv-datenaustausch.de/technische_standards_1/technische_standards.jsp) as [attachment 16](https://www.gkv-datenaustausch.de/media/dokumente/standards_und_normen/technische_spezifikationen/Anlage_16.pdf) under `Security Schnittstelle (SECON)` section.
Once the certificates are issued, checkout the [prerequisites from the Readme](https://github.com/alex-therapeutics/diga-api-client#prerequisites) to be able to use them with the api client.


# FAQ

## What can I do if I want to make requests but my DiGA is not approved yet and I therefore do not have a diga id?

Just sent some 5 digit number as part of the `nutzdaten`, e.g. `12345`.

## What can I do if I dont know the IK number yet and still want to test the client?

You will need an IK number to request the certificate for encryption and decryption. Therefore, it is a must-have.

## Are there solutions for non-java users?

So far, there are no other clients available.
A solution could be to create a docker image which exposes the java client via an api.
Additionally, we hope that this documentation provides enough information so that clients in other languages can be developed.

## My DiGA (app) is only valid for 30 days. Can users request a fresh code from their insurer while their existing code is still valid?

**TODO** Hopefully, users want to continue using your DiGA after their initial prescription code expired.

- Can they already request a new code while still being active?
- How will the verification and billing api handle these cases?
- Is the `TagDerLeistungserbringung` the date on which the request is made or is this set by the insurer when issuing the prescription code?

# Glossary

| Term (German original)  | Short description in English  |
| ----------------------- | ----------------------------- |
| Institutionskennzeichen | Unique of a DiGA manufacturer |

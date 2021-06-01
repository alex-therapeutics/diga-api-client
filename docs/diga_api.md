# Overview

Manufacturers of digitial health applications (DiGA) can apply for a fast track at [bfarm](https://www.bfarm.de/EN/MedicalDevices/DiGA/_node.html) to place their solutions in a directory of apps which can be prescribed by doctors to patients. By showing evidence that the apps improve patients health and by obtaining medical device certifications, it can be assured that the apps are of high quality. On the other side, DiGA manufacturers get reimbursed by health insurances when their apps are prescribed to patients. The prescription process is build around a 16-character prescription code which can be used to activate the apps but which is also required to send the invoices. This repository holds code to:

- verify that user-entered prescription codes are valid
- create and send invoices to insurance companies based on prescription codes.

This documentation complements the code by describing the DiGA api and the full flow on a higher level. With this documentation we hope that other DiGA manufacturers can understand the required steps to verify and reimburse prescription codes without having to read through the (mainly German) official documentation. Also we hope that it will facilitate writing similar solutions in other programming languages if necessary. Finally, it can be used as a place to collect questions and answers around the DiGA api.

## Summary

- user enters prescription code in the DiGA (app)
- insurance information is extracted from the code
- a request is made to the api of the insurance to verify the code
  - response contains `Tag der Leistungserbringung` which is the day a user started to use the app
  - this is required for the billing process
- another request is made for reimbursing the code

## The prescription code

Patients receive 16-character prescription codes by their insurances. The codes are used to activate the DiGA apps and for creating invoices for billing. The structure of the code is defined [here](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_Anlage_1_Technische_Anlage_zur_RL_V1.1_20210225.pdf) on page 9 (German).

| Krankenkassencode/Kostenträgerkürzel | Version     | Unique code for insurance | Checksum    |
| ------------------------------------ | ----------- | ------------------------- | ----------- |
| 2 characters                         | 1 character | 12 characters             | 1 character |

The first two characters are an identifier for the insurance. They can be used to get additional information from a mapping (xml) file which can be downloaded [here](https://kkv.gkv-diga.de/).

The other parts contain a version (`A`) and a unique code for the patient & insurance. The last character is a checksum which is described [here](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/Anhang3_Freischaltcode_Berechnungsregel_Pruefziffer_V1.0.pdf) with an open source implementation for different languages on [Github](https://github.com/bitmarck-service).

Codes for testing can be downloaded as xlsx [here](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/Pseudo-Codes.xlsx). Some of these codes are invalid with error codes defined [here](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_Anhang5_Fehlerausgaben_V1.0.1_20210423.pdf).

| Rückgabewert | Freischaltcode   | Fehlertext                    | Erläuterung                                                                                                                                                                            |
| ------------ | ---------------- | ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 0            | 77AAAAAAAAAAAAAX | Anfrage gültig                | Aktuelles Tagesdatum für "Tag_der_Leistungserbringung" aus EDFC0-basis_2.0.0.xsd verweden. Für die DiGANr laut aus EDFC0-basis_2.0.0.xsd sind die letzten 3-Stellen mit 000 anzugeben. |
| 100          | 77AAAAAAAAAAADEV | Freischaltcode abgelaufen     | Fachlicher Fehler. Der Fehler wird ausgegebene wenn der Code zeitlich abgelaufen ist.                                                                                                  |
| 101          | 77AAAAAAAAAAADFF | Freischaltcode storniert      | Fachlicher Fehler.                                                                                                                                                                     |
| 102          | 77AAAAAAAAAAADGE | Fresichaltcode nicht gefunden | Fachlicher Fehler. Der Fehler wird ausgegeben wenn bspw. die Zuordnung des Freischaltcodes zur DiGA nicht stimmt.                                                                      |
| 200          | 77AAAAAAAAAAAGIS | Anfrage oder Datei ungültig   | Die Anfrage oder die Datei konnte nicht verarbeitet werden. Der Fehler wird bspw. bei einer Schemaverletzung ausgegeben.                                                               |
| 201          | 77AAAAAAAAAAAGJC | Serverfehler                  | Technischer Fehler. Der Fehler wird bspw. bei einem Übertragungsfehler ausgegeben.                                                                                                     |
| 202          | 77AAAAAAAAAAAGKD | Speicherfehler                | Technischer Fehler. Der Fehler wird bspw. bei einem Datenbankfehler ausgegeben.                                                                                                        |

### Mapping file for insurances

Data for one insurance is listed below.

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

The file can be updated by insurances which requires keeping the mapping file used for the client up-to-date. The field `Kostentraegerkuerzel` is what can be used to find insurance information based on the prescription code. The insurance information lists a field `Endpunkt_Kommunikationsstelle`. This is the base url for the endpoint for a specific insurance. There is no central diga endpoint for verifying and reimbursing DiGA apps and handling might differ among insurances. Some apis might also not support billing as of now. Invoices in these cases must be sent by email or post.

Although insurances use their own apis, they do follow an openapi specification for the request which can be found [here](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/digaSP_1_0_05.yaml). This [documents](https://github.com/alex-therapeutics/diga-api-client/blob/main/ENDPOINT_STATUS.md) lists which apis are currently working with the client.

## Request format

According to the openapi specification the request contains 4 different parameters which are sent with `ContentType: multipart/form-data`:

- `iksender` - Institutionskennzeichen (IK) of the DiGA manufacturer (an id required for payments)
- `ikempfaenger` - IK of the insurance which can be found in the mapping file (`Kostentraegerkennung` - [section 5.2.2](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/DiGA_Anlage_1_Technische_Anlage_zur_RL_V1.1_20210225.pdf))
- `verfahren` - a code which discriminates requests for code verification and reimbursement
- `nutzdaten` - additional information for the request, including the prescription code
  - the `nutzdaten` are described later in more detail as this is the tricky bit of the request

### Institutionskennzeichen - IK numbers

Detailed information about the IK numbers can be found [here](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/GR_IK_2020-06-01.pdf) (German only).

> Das IK ist ein eindeutiges Merkmal für die Abrechnung medizinischer und rehabilitativer
> Leistungen mit den Trägern der Sozialversicherung [...] Es gilt damit als offizielles Kennzeichen der Leistungsträger und Leistungserbringer
> im Schriftverkehr und für Abrechnungszwecke (§ 293 SGB V).

IK numbers are unique identifiers, 9 digits long, which are required for the billing of medical services and used as official identifiers. Therefore, every DiGA manufacturer must request an IK number. An IK number is also required when [requesting a certificate from ITSG](https://www.itsg.de/produkte/trust-center/zertifikat-beantragen/) which is required for encrypting and decrypting the `nutzdaten` of the request. IK numbers might be updated when the name, address, or billing address of a company changes.

The linked document also holds more information about how the numbers are structured and more importantly, how they can be requested.

#### Requesting an IK number

According to section 2.2.1

> Die Vergabe, die Änderung der Daten [...] erfolgen auf Antrag. Der Erfassungsbeleg kann bei der ARGE•IK sowie bei jedem Sozialversicherungsträger angefordert oder auf der Internetseite der ARGE•IK (www.arge-ik.de) heruntergeladen werden.

You need to request the IK or changes using a form. You can check the [ARGE-IK](https://www.dguv.de/arge-ik/index.jsp) website for more information (German). Specific information for requesting the number can be found in the [Antrag](https://www.dguv.de/arge-ik/antrag/index.jsp) tab.
The form can be sent by email and a blueprint can be downloaded [here](https://www.dguv.de/medien/arge-ik/downloads/erfass.pdf).

[Additional information](https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/sonstige_leistungserbringer/20210401_Broschuere_TP5.pdf) which also contains a section about requesting certificates from ITSG trust center.

### Verfahren

The `verfahren` parameter consists of 5 characters describing the kind of the request. The first character is either `T` or `E` denoting `Testdaten` (test data) or `Echtdaten` (real data) - [source](https://www.gkv-datenaustausch.de/media/dokumente/standards_und_normen/technische_spezifikationen/Anlage_4_-_Verfahrenskennungen.pdf).

The next 3 characters are used to discriminate between:

- verification - DFC (**D**iga **F**reischalt**c**ode)
- billing - DRE (**D**iga **Re**chnung) [source](https://github.com/bitmarck-service/validator-configuration-diga).

The last one is for versioning which leaves us with the following values:

- `EDFC0` and `TDFC0` for verification
- `EDRE0` and `TDRE0` for billing.

### Nutzdaten

**TODO**

Must be signed using a certificate and then encoded to byte array

## Request details

**TODO**

Processing time constraints: Mean value 10000[msec], 95% quantile [msec] 15000

### Code verification requests

**TODO**

### Billing requests

Invoices must be created using the `XRechnung` standard. There might also be dedicated account/book-keeping software which allows creating and sending invoices according to this standard, so that billing might also be handled outside of the deployed system. However, the client supports sending invoices by mail. It is up to the users of the client to define what should happen with the response from the diga api. E.g. invoices are likely to be required for accounting and therefore must be stored. Also handling of apis which do not support billing yet, must be defined.

**TODO**

itplr-kosit/validator: Validates XML documents with XML Schema and Schematron
Can be done via email or mail in the beginning. See mapping table for insurance-specific information

Die Abrechnung hat insbesondere folgende Bestandteile:

- Freischaltcode / Rezeptcode
- Eindeutige DiGA Nummer gemäß dem Verzeichnis nach §139e SGB Vs
- Verordnungsdauer
- Informationen zum Rechnungssteller und zum Kostenträger

### Encryption

**TODO**

https://github.com/alex-therapeutics/secon-keystore-generator
https://www.gkv-datenaustausch.de/media/dokumente/standards_und_normen/technische_spezifikationen/Anlage_16.pdf
Page 52: Die CA (Trust Center) generiert auf Anfrage ein Zertifikat das u.a. den Namen des Systemteilnehmers, den öffentlichen Schlüssel sowie den Namen des Zertifikatserzeugers enthält

https://www.itsg.de/produkte/trust-center/zertifikat-beantragen/

## FAQ

### What can I do if I dont know the IK number yet and still want to test the client?

### What can I do if I want to make requests but my DiGA is not approved yet and therefore does not have a diga id?

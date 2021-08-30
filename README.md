# DiGA API Client

This library provides a client which interacts with the DiGA ([Digital Health Application](https://www.bfarm.de/EN/MedicalDevices/DiGA/_node.html)) API to:
- Validate DiGA codes against DiGA API endpoints
- Send invoices to DiGA API endpoints using the required XRechnung format

The goal is to deliver a simple and easy to use way for DiGA manufacturers to integrate with the DiGA API provided by health insurance companies (or their service providers).

The DiGA API is currently in an early stage and lacks documentation - especially in English - which makes interacting with it a daunting task. 
We hope that creating an open source project will be a way for DiGA manufacturers to work together and share our experiences to maintain a stable DiGA API integration.
[Contributions](CONTRIBUTING.md) are very welcome!

Currently, the DiGA API endpoints seem to differ from eachother in some respects, so it will take an effort to create a stable integration against all of them.
If you discover a case which has not been considered yet in this library, please open an [Issue](https://github.com/alex-therapeutics/diga-api-client/issues/new) here on GitHub -  and perhaps also consider [contributing](CONTRIBUTING.md) with a Pull Request to handle it! :)

Also, if you have information on how to solve existing issues but don't want to get involved with actual coding, please don't hesitate to comment on the issue anyway, to make it easier for someone else to solve! 
All contributions are welcome, we do not expect you to commit code if you do not want to.

## Project Status
The first version of the project has been released. Currently, the DiGA code validation works against 98/104 health insurance companies and invoicing works against 99/104 companies. However, there are still issues with a few endpoints. You can track this further [here](ENDPOINT_STATUS.md) and there is also an issue for each non-working endpoint.
For insurance companies which do not support API invoicing (5/104) the library will return a response which indicates what to do next (invoice via email or post).

## Get Started

To communicate with the DiGA api companies **must request an IK number and certificates for encryption**.
These two prerequisites cannot be requested in parallel and it takes up to four weeks to get them.
Therefore, make sure to request them as early as possible.
Please see the [docs](docs/diga_api.md#Requesting-an-IK-number) folder for more information.

### Prerequisites
* PKCS12 keystores for the certificates of all the health insurance companies as well as your private key and certificate that you applied for from ITSG.
  You must give your private key/certificate an alias to use it with the library, and all health insurance certificates must have the company's IK number as an alias
  in the form _IK123456789_. You can create the keystore with a single command using the [secon-keystore-generator](https://github.com/mawendo-gmbh/secon-keystore-generator) tool.
  Use the command mentioned [here](https://github.com/mawendo-gmbh/secon-keystore-generator#public-certificates-and-your-private-certificate-keystore) and you will
  get a single keystore which you can use as both the private key store file and public health insurance public key store file (see Usage).
* The XML mapping file for the health insurance companies which contains information on endpoints, IK numbers, code prefixes, etc.
  [This page](https://kkv.gkv-diga.de/) should provide an up-to-date file. __Important!__ read [here](https://github.com/alex-therapeutics/diga-api-client/wiki/Modifying-the-insurance-company-mapping-file-(Krankenkassenverzeichnis_DiGA.xml)) for the modifications you have to make to this file.
* Java 11 or higher. Do you need Java 8? Submit an issue and tell us! There is still room to make such changes.

### Installation
Import it using Maven in your `pom.xml`:
```xml
<dependency>
    <groupId>com.alextherapeutics</groupId>
    <artifactId>diga-api-client</artifactId>
    <version>1.2.0</version>
</dependency>
```
It is also uploaded to Github packages if you wish to use that

## Usage

### Initializing the client

This example assumes you put both your private certificate and the insurance companies certificates in `keystore.p12`,
that the XML mapping file is called `mappings.xml`, and that you put these files in the `resources` folder.

Note that there is quite a lot of information required to _instantiate_ the client, however, it is designed this way to
enable _each individual request_ afterwards to require as little information as possible - as demonstrated later.

```java
// in Main.java
var mappingFile = Main.class.getClassLoader().getResourceAsStream("mappings.xml");
var healthCompaniesKeyStore = Main.class.getClassLoader().getResourceAsStream("keystore.p12");
var privateKeyStore = Main.class.getClassLoader().getResourceAsStream("keystore.p12"); // you need one inputstream for each

var apiClientSettings = DigaApiClientSettings.builder() // settings required for the client to operate
        .healthInsuranceMappingFile(mappingFile)
        .privateKeyStoreFile(privateKeyStore)
        .healthInsurancePublicKeyStoreFile(healthCompaniesKeyStore)
        .privateKeyStorePassword("my-keystore-password")
        .privateKeyAlias("my-private-key-alias") // you must create this when creating the keystore
        .healthInsurancePublicKeyStorePassword("my-keystore-password")
        .build();
        
var digaInformation = DigaInformation.builder() // information about your DiGA and your company required to easily
                                                // create invoices and send requests to the API
        .digaName("my-DiGA-common-name")
        .digaId("my-DiGA-id-or-any-random-5-digits") // if you arent accepted as DiGA yet, just put 12345
        .manufacturingCompanyName("my-company-name")
        .manufacturingCompanyIk("my-IK-number")
        .netPricePerPrescription(new BigDecimal(100)) // net price per diga code validated
        .applicableVATpercent(new BigDecimal(19)) // how much VAT should be applied to the invoices
        .manufacturingCompanyVATRegistration("DE 123 456 789")
        .contactPersonForBilling(
            DigaInformation.ContactPersonForBilling.builder()
                .fullName("Sven Svensson")
                .phoneNumber("+46 70 123 45 67")
                .emailAddress("svensvensson@awesomedigacompany.com")
                .build()
        )
        .companyTradeAddress(
            DigaInformation.CompanyTradeAddress.builder()
                .adressLine("Diga Street 1")
                .postalCode("123 45")
                .city("Digatown")
                .countryCode("DE")
                .build()
        )
        .build();

var apiClient = new DigaApiClient(apiClientSettings, digaInformation);
```

### Using the client
```java
var digaCode = "real-16-character-code";

var codeValidationResponse = apiClient.validateDigaCode(digaCode); // small API for code validation

if (codeValidationResponse.isHasError()) {
  // handle error
}

var invoice = DigaInvoice.builder()
        .invoiceId("1") // unique invoice IDs
        .validatedDigaCode(digaCode)
        .digavEid(codeValidationResponse.getValidatedDigaveid())
        .dateOfServiceProvision(codeValidationResponse.getDayOfServiceProvision())
        .build();

var invoiceResponse = apiClient.invoiceDiga(invoice); // small API for invoicing

if (invoiceResponse.isHasError()) {
    // handle error
} else if (invoiceResponse.isRequiresManualAction()) {
    switch (invoiceResponse.getInvoiceMethod()) {
        case EMAIL:
            var targetEmail = invoiceResponse.getInsuranceCompanyInvoiceEmail();
            var generatedInvoice = invoiceResponse.getGeneratedInvoice();
            // handle emailing the invoice to the insurance company
            break;
        case POST:
            // handle having to post the invoice
            break;
    }
}

log.info("Successfully validated and invoiced a DiGA code!");

sendInvoiceToAccounting(invoiceResponse.getGeneratedInvoice()); // for accounting or troubleshooting purposes, you can access the generated invoice in the response
```

You can also send test requests like this

```java
// send a test request to the insurance company with prefix "BY" which should be be processed as valid by the API
var testCodeValidationResponse = apiClient.sendTestRequest(DigaApiTestCode.VALID, "BY"); 

// send a test invoice to the insurance company with prefix "BY". note that test bills never return as completely valid by the APIs 
// at the moment. you will at best get a response like "code not found" or "wrong check digit"
var testInvoiceResponse = apiClient.sendTestInvoiceRequest(
        DigaInvoice.builder().invoiceId("1").validatedDigaCode(DigaApiTestCode.VALID.getCode()).build(),
        "BY"
);
```

### Gotchas
__Note__: Make sure you do __not__ put your keystore file in a _filtered_ resource location, because it will mess up the file when importing it as a resource. To be clear, if you have something like:
```xml
<resources>
  <resource>
    <directory>src/main/resources</directory>
    <filtering>true</filtering>
  </resource>
</resources>
```
and your `.p12` file is located in `src/main/resources`, then you will get a `SECONException`. Solve this by putting your file somewhere else or [exclude it from filtering](https://stackoverflow.com/a/34750851/6428035).


### Advanced Usage

You can provide your own implementations of interfaces in the library if you need custom logic. For example, you may wish
to use a different HTTP client to make the requests. Here's an example:

Write your custom implementation:
```java
public class CustomDigaHttpClient implements DigaHttpClient {

    public CustomDigaHttpClient() {
        // initialize your http library and ssl configuration
    }

    @Override
    public DigaApiHttpResponse post(DigaApiHttpRequest request) throws DigaHttpClientException {

        // make the http call using the information provided in the 'request' object

        return DigaApiHttpResponse.builder()
                // .. set response fields
                .build();
    }
}
```

Instantiate the API client with your custom implementation:
```java
public DigaApiClient createCustomApiClient() {
    var customHttpClient = new CustomDigaHttpClient();
    return DigaApiClient.builder()
        .httpClient(customHttpClient)
        .healthInsuranceDirectory( ... )
        .codeParser( ... )
        .xmlRequestWriter( ... )
        .xmlRequestReader( ... )
        .encryptionFactory( ... )
        .digaInformation( .. )
        .build();
}
```

When using the builder you are required to provide an implementation for all the interfaces. For the ones you don't want to custom write,
you can just instantiate the default implementations. See the code documentation for further details on what each interface does and how to instantiate the default implementations.
When writing custom implementations it can be a good idea to look at the default implementation first to see what it needs to do to work properly.

## Versioning

We use [semantic versioning](https://semver.org/)

## Long-term plans & ideas

These are some interesting ideas for futher development, if there is interest among users and the base integration is stable:

- Create a Spring extension for dependency injection
- Create a standalone docker container which can run separately and handle DiGA API integrations for non-Java users.

## FAQ
Q: I´m running a test invoice request (Verfahrenskennung `TDRE0`) targetting a insurance company that uses the services of Bitmarck (e.g. 'CM', which is DAK). In the response I receive an error:

> Anfrage oder Datei ungültig: NonEmptyList(de.bitmarck.bms.diga.activation.core.model.ModelError$InvalidCreditorCorpId: Creditor corp id must match regex \".*\" and not match regex \"10.*|660500345\", but is: 123456789), with code: INVER-1, at xPathLocation: rsm:CrossIndustryInvoice

What can I do to resolve this?

A: According to a Bitmarck service employee this is normal (state of 30.04.2021). A test invoicing is currently not supported by Bitmarck. The only valid test is to use the https://github.com/itplr-kosit/validator with DiGA configuration and to run the invoicing process with a real DiGA code and Verfahrenskennung `EDRE0`.

## Maintainers

This project is currently maintained by the developer team at [Alex Therapeutics](https://www.alextherapeutics.com/), a Swedish company that develops software medical devices. Contact [max@alextherapeutics.se](mailto:max@alextherapeutics.com) for inquiries which do not belong on Github. If you or your company wishes to help maintain this project, please let us know.

## License

Distributed under the [Apache-2.0](LICENSE) license. Copyright is held by Alex Therapeutics AB and individual contributors.

XML schema files (.xsd) are copied from other repositories and retain their original license and copyright terms. You can find that information and where the files were taken from in the header of each individual file.

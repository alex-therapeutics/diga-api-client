# DiGA API Client

This library provides a client which interacts with the DiGA ([Digital Health Application](https://www.bfarm.de/EN/MedicalDevices/DiGA/_node.html)) API to:
- Validate DiGA codes against DiGA API endpoints
- ~~Send bills to DiGA API endpoints using XRechnung~~ (In progress)

The goal is to deliver a simple way for DiGA manufacturers to integrate with the DiGA API provided by health insurance companies (or their service providers).

The DiGA API is currently in an early stage and lacks documentation - especially in English - which makes interacting with it a daunting task. 
We hope that creating an open source project will be a way for DiGA manufacturers to work together and share our experiences to maintain a stable DiGA API integration.
[Contributions](CONTRIBUTING.md) are very welcome!

Currently, the DiGA API endpoints seem to differ from eachother in some respects, so it will take an effort to create a stable integration against all of them.
If you discover a case which has not been considered yet in this library, please open an [Issue](https://github.com/alex-therapeutics/diga-api-client/issues/new) here on GitHub -  and perhaps also consider [contributing](CONTRIBUTING.md) with a Pull Request to handle it! :)

Also, if you have information on how to solve existing issues but don't want to get involved with actual coding, please don't hesitate to comment on the issue anyway, to make it easier for someone else to solve! 
All contributions are welcome, we do not expect you to commit code if you do not want to.

## Project Status
The project is pre-first-release. Currently, the DiGA code validation works against the `diga.bitmarck-daten.de`, which
is the endpoint used for many of the health insurance companies - a good first step. However, there are issues with other endpoints.
Integration with the billing process with XRechnung has not been started yet.

## Get Started
### Prerequisites
* A PKCS12 keystore containing your certificate and private key that you applied for from ITSG. You must give your private key/certificate an alias
  to use it with the library.
* A PKCS12 keystore containing all the certificates of the health insurance companies. This can be in the same file as the private certificate. All certificates __must__
  be imported into the keystore with the company's IK number as an alias - either in the form _IK123456789_  or _123456790_. We are working on a tool or better documentation 
  to make this process easier.
* The XML mapping file for the health insurance companies which contains information on endpoints, IK numbers, code prefixes, etc.
  [This page](https://kkv.gkv-diga.de/) should provide an up-to-date file.
* Java 11. Do you need Java 8? Submit an issue and tell us! There is still room to make such changes.

### Installation
The plan is to release version `1.0.0` of this library to maven central when it is stable enough (see [roadmap](#roadmap-to-v1-release)). 
Until then you need to build it to your local maven repository before importing it into your project.
```shell
git clone https://github.com/alex-therapeutics/diga-api-client
cd diga-api-client
mvn clean install
```
Then, you can import it in your `pom.xml`:
```xml
<dependency>
    <groupId>com.alextherapeutics</groupId>
    <artifactId>diga-api-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Usage

This example assumes you put both your private certificate and the insurance companies certificates in `keystore.p12`,
that the XML mapping file is called `mappings.xml`, and that you put these files in the `resources` folder.

```java
// in Main.java
var mappingFile = Main.class.getClassLoader().getResourceAsStream("mappings.xml");
var keyStore = Main.class.getClassLoader().getResourceAsStream("keystore.p12");

var apiClientSettings = DigaApiClientSettings.builder()
            .healthInsuranceMappingFile(mappingFile)
            .privateKeyStoreFile(keyStore)
            .healthInsurancePublicKeyStoreFile(keyStore)
            .privateKeyStorePassword("my-keystore-password")
            .privateKeyAlias("my-private-key-alias") // you must create this when creating the keystore
            .healthInsurancePublicKeyStorePassword("my-keystore-password")
            .senderIkNUmber("my-IK-number")
            .senderDigaId("my-DiGA-id-or-any-random-5-digits") // if you arent accepted as DiGA yet, just put 12345
            .build()
var apiClient = new DigaApiClient(apiClientSettings);

var response = apiClient.validateDigaCode("real-16-character-code"); // clean API for code validation
```

You can also send a test request like this

```java
// send a test request to the insurance company with prefix "BY" which should be be processed as valid by the API
var testResponse = apiClient.sendTestRequest(DigaApiTestCode.VALID, "BY"); 
```

__Note__: Make sure you do __not__ put your keystore file in a _filtered_ resource location, because it will mess up the file when importing it as a resource. To be clear, if you have something like:
```xml
<resources>
  <resource>
    <directory>src/main/resources</directory>
    <filtering>true</filtering>
  </resource>
</resources>
```
and your `.p12` file is located in src/main/resources, then you will get a `SECONException`. Solve this by putting your file somewhere else or [exclude it from filtering](https://stackoverflow.com/a/34750851/6428035).


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
        .senderIk( ... )
        .build();
}
```

When using the builder you are required to provide an implementation for all of the interfaces. For the ones you don't want to custom write,
you can just instantiate the default implementations. See the code documentation for further details on what each interface does and how to instantiate the default implementations.
When writing custom implementations it can be a good idea to look at the default implementation first to see what it needs to do to work properly.

## Roadmap to V1 release

These goals have to be completed before V1 release

- [ ] Sending test codes to a majority of endpoints has to work
- [ ] Sending real code validation requests to a majority of endpoints has to return decryptable respones, even with faulty codes (because some evidence suggest that test requests are sometimes processed differently from real requests by some endpoints, we cannot test with only test codes unfortunately)
- [ ] Sending XRechnung bills to Bitmarck's API, which as of the time of writing is the only API accepting them, works.

More specific issues on these topics can be found in the [version 1 release project](https://github.com/alex-therapeutics/diga-api-client/projects/1)

## Versioning

We use [semantic versioning](https://semver.org/)

## Long-term plans & ideas

These are some interesting ideas for futher development, if there is interest among users and the base integration is stable:

- Create a Spring extension for dependency injection
- Create a standalone docker container which can run separately and handle DiGA API integrations for non-Java users.

## Maintainers

This project is currently maintained by the developer team at [Alex Therapeutics](https://www.alextherapeutics.com/), a Swedish company that develops software medical devices. Contact [max@alextherapeutics.se](mailto:max@alextherapeutics.com) for inquiries which do not belong on Github. If you or your company wishes to help maintain this project, please let us know.

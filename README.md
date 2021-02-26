# diga-api-client

This library provides a client which interacts with the DiGA API to:
- Validate DiGA codes against DiGA API endpoints
- ~~Send bills to DiGA API endpoints using XRechnung~~ (TODO)

The goal is to deliver a simple way for DiGA manufacturers to integrate with the DiGA API provided by health insurance companies (or their service providers).

The DiGA API is currently in an early stage and lacks documentation - especially in English - which makes interacting with it a daunting task. 
We hope that creating an open source project will be a way for DiGA manufacturers to work together and share our experiences to maintain a stable DiGA API integration.
Contributions are very welcome!

Currently, the DiGA API endpoints seem to differ from eachother in some respects, so it will take an effort to create a stable integration against all of them.
If you discover a case which has not been considered yet in this library, please open an Issue here on GitHub -  and perhaps also consider contributing with a Pull Request to handle it :)


## Get Started
### Prerequisites
* A PKCS12 keystore containing your certificate and private key that you applied for from ITSG (TODO: link)
* A PKCS12 keystore containing all the certificates of the health insurance companies. This can be in the same file as the private certificate as well. (TODO link)
* The XML mapping file for the health insurance companies which contains information on endpoints, IK numbers, code prefixes, etc. (TODO link)
### Installation
The plan is to release V1 of this library to maven central when it is stable enough. 
Until then you need to build it to your local maven repository before importing it into your project.
```shell
git clone https://github.com/alex-therapeutics/diga-api-client
cd diga-api-client
mvn clean install
```
After doing this, you can import it in your `pom.xml`:
```xml
<groupId>com.alextherapeutics</groupId>
<artifactId>diga-api-client</artifactId>
<version>1.0-SNAPSHOT</version>
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

// TODO - show builder variation

// TODO - consider putting up the tool for importing all certs and referring to it from here

// TODO - make list of goals to achieve before V1

## Roadmap to V1 release

These things have to be completed before V1 release

- [ ] Sending test codes to 75% of endpoints has to work
- [ ] Sending faulty but real code validation requests to 75% of endpoints has to return decryptable respones (because some evidence suggest that test requests are sometimes processed differently from real requests by some endpoints..)
- [ ]

## Maintaners

The project is currently maintained by the developer team at Alex Therapeutics, a Swedish company that develops software medical devices.. Contact max@alextherapeutics for inquiries which do not belong on Github. If you or your company wishes to help maintain this project, please let us know.

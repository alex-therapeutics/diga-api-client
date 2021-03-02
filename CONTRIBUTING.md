# Contributing to the DiGA API Client

Thank you for your interest in contributing to the DiGA API client!

This document describes how to set up a development environment and submit your code contributions. If you are not ready to
submit code just yet, submitting an issue, sharing information on how to solve one of the existing issues, or improving 
the documentation are also valuable contributions.

## Preparing

If you are not working on one of the existing Issues, open a new issue explaining what you are trying to do. It helps
others know that the work is in progress, so we don't duplicate work. It is also a good idea to see if there already
exists an issue similar to yours. If it's a very small change, like smaller documentation changes, you don't need an issue.

Then, fork the repository on GitHub or checkout a new branch, and let's get started!

## Prerequisites

You need a development environment for Java 11 or higher, for example an IDE like IntelliJ, Eclipse, etc.
If you want to run the client against a DiGA API, you will need your own client certificate and private key stores for
encrypting data and establishing connections with the APIs. It is unfortunately not possible for us (the maintainers)
to provide working certificates for testing as we only have our private certificate which we cannot share, for obvious reasons.

## Running the code

As this is a library which is not meant to run standalone (yet), there is no main class packaged with it. There are a
couple of options for running the code depending on what you want to do: 
  - If you are making small inter-library changes which do not require establishing a connection with a DiGA API, then perhaps you can write a JUnit test which runs
the part of the code that you are working on.
  - If you want to run the client end-to-end, then you need to provide a main class. Any file named Main.java is gitignored
in this project, so the easiest thing is to create such a file (`Main.java`) in the project and initialize the client there.
    Something like this:
    ````java
    public class Main {
        public static void main(String[] args) {
            var senderIK = "my-company-IK";
            var digaId = "12345"; // or your real ID if you have one
            var mappingFile = Main.class.getClassLoader().getResourceAsStream("Krankenkassenverzeichnis_DiGA.xml");
            var keystore = Main.class.getClassLoader().getResourceAsStream("keystore.p12");
            var keystorePass = "my-keystore-password";
            var privateKeyAlias = "my-private-key-alias-in-keystore";

            var apiClient = new DigaApiClient(
                    DigaApiClientSettings.builder()
                            .healthInsuranceMappingFile(mappingFile)
                            .privateKeyStoreFile(keystore)
                            .healthInsurancePublicKeyStoreFile(keystore)
                            .privateKeyStorePassword(keystorePass)
                            .privateKeyAlias(privateKeyAlias)
                            .healthInsurancePublicKeyStorePassword(keystorePass)
                            .senderIkNUmber(senderIK)
                            .senderDigaId(digaId)
                            .build()
            );

            var response = apiClient.sendTestRequest(DigaApiTestCode.VALID, "BY"); // this sends a test request to the company with prefix "BY"
            System.out.println(response.toString());
        }
    }

    ````

## Making a Pull Request

When you are done, create a Pull Request against the `main` branch. Describe what you have done and what you are trying to fix.
If the PR closes an issue, refer to that as well.

Next, a maintainer will review your PR. If changes are requested, make your changes and commit them to the same branch 
you were working on, and re-request a review.

Finally, once your PR is approved, a maintainer will merge it into the master branch. Thank you for your contribution!

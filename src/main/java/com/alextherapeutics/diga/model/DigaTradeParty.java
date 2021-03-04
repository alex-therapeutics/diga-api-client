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

package com.alextherapeutics.diga.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Information for the seller or buyer (a "trade party") of a DiGA prescription.
 * Note that not all fields are required and may differe from buyer and seller.
 * See test bill at https://github.com/bitmarck-service/validator-configuration-diga/blob/master/src/test/resources/dre0/xrechnung-2.0-richtig.xml
 */
// TODO maybe this is better as an inner class to xml writer
    // and we separate buyer info and seller info in our public model instead, in a similar way to this
    // but with other info too which is not "tradeparty" specific but bill specific
@Builder
@Getter
public class DigaTradeParty {
    @NonNull
    private String companyId;
    @NonNull
    private String companyIk;
    @NonNull
    private String companyName;
    private String taxRegistration;
    private DigaTradePartyContactPerson contactPerson;
    @NonNull
    private DigaTradeParty.DigaTradePartyPostalAddress postalAddress;

    @Builder
    @Getter
    public static class DigaTradePartyContactPerson {
        @NonNull
        private String fullName;
        private String telephoneNumber;
        private String emailAddress;
    }

    @Builder
    @Getter
    public static class DigaTradePartyPostalAddress {
        @NonNull
        private String postalCode;
        @NonNull
        private String adressLine;
        @NonNull
        private String city;
        @NonNull
        private String countryCode;
    }
}

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

import com.alextherapeutics.diga.DigaCodeParser;
import com.alextherapeutics.diga.DigaCodeValidationException;
import com.alextherapeutics.diga.DigaHealthInsuranceDirectory;
import com.alextherapeutics.diga.model.DigaBillingInformation;
import com.alextherapeutics.diga.model.DigaCodeInformation;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Parses a DiGA Code
 */
@AllArgsConstructor
public class DigaCodeDefaultParser implements DigaCodeParser {
    private DigaHealthInsuranceDirectory healthInsuranceDirectory;
    @Override
    public DigaCodeInformation parseCodeForValidation(String code) throws DigaCodeValidationException {
        var parsedCode = parseCode(code);
        var healthInsuranceInformation = healthInsuranceDirectory.getInformation(parsedCode.healthInsuranceCode);
        // TODO null check and throw exception?
        return DigaCodeInformation.builder()
                .endpoint(healthInsuranceInformation.getEndpunktKommunikationsstelle())
                .insuranceCompanyIKNumber(healthInsuranceInformation.getIKAbrechnungsstelle())
                .insuranceCompanyName(healthInsuranceInformation.getNameDesKostentraegers())
                .fullDigaCode(code)
                .personalDigaCode(parsedCode.healthInsuranceIndividualCode)
                .build();
    }
    @Override
    public DigaBillingInformation parseCodeForBilling(String code) throws DigaCodeValidationException {
        var parsedCode = parseCode(code);
        var healthInsuranceInformation = healthInsuranceDirectory.getInformation(parsedCode.healthInsuranceCode);
        return DigaBillingInformation.builder()
                .endpoint(healthInsuranceInformation.getEndpunktKommunikationsstelle())
                .insuranceCompanyIKNumber(healthInsuranceInformation.getIKAbrechnungsstelle())
                .buyerCompanyCreditorIk(healthInsuranceInformation.getIKDesRechnungsempfaengers())
                .buyerCompanyId(healthInsuranceInformation.getNameDesKostentraegers())
                .insuranceCompanyName(healthInsuranceInformation.getNameDesKostentraegers())
                .buyerCompanyPostalCode(healthInsuranceInformation.getPLZ())
                .buyerCompanyAddressLine(healthInsuranceInformation.getStrassePostfach() + " " + healthInsuranceInformation.getHausnummerPostfachnummer())
                .buyerCompanyCity(healthInsuranceInformation.getOrt())
                .build();
    }

    // according to
    // https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/digitale_gesundheitsanwendungen/technische_anlagen_aktuell_7/Anlage_1_Technische_Anlage_zur_RL_V1.0.pdf
    private ParsedDigaCode parseCodeString(String codeString) {
        return ParsedDigaCode.builder()
                .healthInsuranceCode(
                        codeString.substring(0,2)
                )
                .version(
                        Character.toString(codeString.charAt(2))
                )
                .healthInsuranceIndividualCode(
                        codeString.substring(3,15)
                )
                .checksum(
                        Character.toString(codeString.charAt(15))
                )
                .build();
    }
    private boolean validateDigaCodeStructure(String code) {
        // TODO - make more check, like regex, the different parts, etc.
        // TODO maybe use checksum to check validity?
        return code != null && code.length() == 16;
    }
    private DigaCodeDefaultParser.ParsedDigaCode parseCode(String code) throws DigaCodeValidationException {
        if (!validateDigaCodeStructure(code)) {
            throw new DigaCodeValidationException("Invalid DiGA code");
        }
        return parseCodeString(code);
    }

    @Builder
    private static class ParsedDigaCode {
        private String healthInsuranceCode;
        private String version;
        private String healthInsuranceIndividualCode;
        private String checksum;
    }
}

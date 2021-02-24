package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaCodeInformation;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Parses DiGA code returning an information object containing information gathered
 * from the insurance company mapping list at https://kkv.gkv-diga.de/
 */
@AllArgsConstructor
public class DigaCodeParser {
    private DigaHealthInsuranceDirectory healthInsuranceDirectory;
    public DigaCodeInformation getTestInfo() {
        return DigaCodeInformation.builder()
                .endpoint("https://diga.bitmarck-daten.de/diga")
                .insuranceCompanyIKNumber("IK104593982")
                .insuranceCompanyName("Bitmarck")
                .fullDigaCode("77AAAAAAAAAAAAAX")
                .build();

    }
    public DigaCodeInformation parseCode(String code) throws DigaCodeValidationException {
        if (!validateDigaCodeStructure(code)) {
            throw new DigaCodeValidationException("Invalid DiGA code");
        }
        var parsedCode = parseCodeString(code);
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
        return code.length() == 16;
    }
    @Builder
    private static class ParsedDigaCode {
        private String healthInsuranceCode;
        private String version;
        private String healthInsuranceIndividualCode;
        private String checksum;
    }
}

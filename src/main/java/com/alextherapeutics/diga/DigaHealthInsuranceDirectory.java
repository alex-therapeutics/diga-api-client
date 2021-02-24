package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.xml.krankenmapping.KostentraegerMappingverzeichnis;
import com.alextherapeutics.diga.model.xml.krankenmapping.KrankenkasseninformationCtp;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Unmarshals the contents of the health insurance data mapping file provided by gkv at
 * https://kkv.gkv-diga.de/ and provides methods for accessing the information.
 */
public class DigaHealthInsuranceDirectory {
    private JAXBContext context;
    private Unmarshaller unmarshaller;
    private Map<String, KrankenkasseninformationCtp> prefixToInformationMap;

    private DigaHealthInsuranceDirectory(InputStream xmlMappingFileContent) throws JAXBException {
        context = JAXBContext.newInstance(KostentraegerMappingverzeichnis.class);
        unmarshaller = context.createUnmarshaller();
        var root = (KostentraegerMappingverzeichnis) unmarshaller.unmarshal(xmlMappingFileContent);
        prefixToInformationMap = root.getKrankenkasseninformation()
                .stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                KrankenkasseninformationCtp::getKostentraegerkuerzel,
                                info -> info
                        )
                );
    }
    public static DigaHealthInsuranceDirectory getInstance(InputStream xmlMappingFileContent) throws JAXBException {
        return new DigaHealthInsuranceDirectory(xmlMappingFileContent);
    }

    /**
     * Get information from the insurance directory based on the company code prefix,
     * or "Kostentraegerkuerzel"
     * @param prefix
     * @return
     */
    public KrankenkasseninformationCtp getInformation(String prefix) {
        return prefixToInformationMap.get(prefix);
    }
}

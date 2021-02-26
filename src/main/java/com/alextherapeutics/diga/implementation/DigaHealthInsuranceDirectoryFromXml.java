package com.alextherapeutics.diga.implementation;

import com.alextherapeutics.diga.DigaHealthInsuranceDirectory;
import com.alextherapeutics.diga.model.xml.KostentraegerMappingverzeichnis;
import com.alextherapeutics.diga.model.xml.KrankenkasseninformationCtp;

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
public class DigaHealthInsuranceDirectoryFromXml implements DigaHealthInsuranceDirectory {
    private JAXBContext context;
    private Unmarshaller unmarshaller;
    private Map<String, KrankenkasseninformationCtp> prefixToInformationMap;

    private DigaHealthInsuranceDirectoryFromXml(InputStream xmlMappingFileContent) throws JAXBException {
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
    public static DigaHealthInsuranceDirectoryFromXml getInstance(InputStream xmlMappingFileContent) throws JAXBException {
        return new DigaHealthInsuranceDirectoryFromXml(xmlMappingFileContent);
    }

    @Override
    public KrankenkasseninformationCtp getInformation(String prefix) {
        return prefixToInformationMap.get(prefix);
    }
}

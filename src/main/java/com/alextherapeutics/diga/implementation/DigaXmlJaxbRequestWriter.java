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

import com.alextherapeutics.diga.DigaUtils;
import com.alextherapeutics.diga.DigaXmlRequestWriter;
import com.alextherapeutics.diga.model.DigaCodeInformation;
import com.alextherapeutics.diga.model.DigaSupportedXsdVersion;
import com.alextherapeutics.diga.model.DigaTradeParty;
import com.alextherapeutics.diga.model.billingxml.*;
import com.alextherapeutics.diga.model.xml.NachrichtentypStp;
import com.alextherapeutics.diga.model.xml.ObjectFactory;
import com.alextherapeutics.diga.model.xml.PruefungFreischaltcode;
import com.alextherapeutics.diga.model.xml.VerfahrenskennungStp;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * An XML writer using JAXB
 */
@Slf4j
public class DigaXmlJaxbRequestWriter implements DigaXmlRequestWriter {
    private String senderIk;
    private String digaId;

    private JAXBContext context;
    private Marshaller marshaller;
    private ObjectFactory objectFactory;
    private DatatypeFactory datatypeFactory;

    private JAXBContext billingContext;
    private Marshaller billingMarshaller;
    private com.alextherapeutics.diga.model.billingxml.ObjectFactory billingFactory;

    @Builder
    public DigaXmlJaxbRequestWriter(@NonNull String senderIk, @NonNull String digaId) throws JAXBException {
        this.senderIk = DigaUtils.ikNumberWithoutPrefix(senderIk);
        this.digaId = digaId;
        init();
    }

    private void init() throws JAXBException {
        objectFactory = new ObjectFactory();
        context = JAXBContext.newInstance(PruefungFreischaltcode.class);
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        datatypeFactory = DatatypeFactory.newDefaultInstance();

        billingFactory = new com.alextherapeutics.diga.model.billingxml.ObjectFactory();
        billingContext = JAXBContext.newInstance(CrossIndustryInvoiceType.class);
        billingMarshaller = billingContext.createMarshaller();
        billingMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    // todo "DigaBillingInformation" as parameter
    public byte[] createBillingRequest() throws IOException, JAXBException {
        // trying to recreate bitmarck's test bill

        // Root
        var invoice = billingFactory.createCrossIndustryInvoiceType();

        // ExchangedDocumentContext
        var exchangedDocumentContext = billingFactory.createExchangedDocumentContextType();
        var guideline = billingFactory.createDocumentContextParameterType();
        var guidelineId = createIdType("urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0");
        guideline.setID(guidelineId);
        exchangedDocumentContext.getGuidelineSpecifiedDocumentContextParameter().add(guideline);
        invoice.setExchangedDocumentContext(exchangedDocumentContext);

        // ExchangedDocument
        var exchangedDocument = billingFactory.createExchangedDocumentType();
        var docId = createIdType("2020-1");
        exchangedDocument.setID(docId);
        var typeCode = billingFactory.createDocumentCodeType();
        typeCode.setValue("380");
        exchangedDocument.setTypeCode(typeCode);
        exchangedDocument.setIssueDateTime(createDateTime(new Date()));
        invoice.setExchangedDocument(exchangedDocument);

        // SupplyChainTradeTransaction
        var transaction = billingFactory.createSupplyChainTradeTransactionType();
        // ---
        var includedSupplyChainTradeLineItem = billingFactory.createSupplyChainTradeLineItemType();

        var associatedDocLineDoc = billingFactory.createDocumentLineDocumentType();
        associatedDocLineDoc.setLineID(createIdType("TEST_POSITION_01"));

        var tradeProduct = billingFactory.createTradeProductType();
        tradeProduct.setGlobalID(createIdType("12345678")); // digaveid
        tradeProduct.setBuyerAssignedID(createIdType("77AAAAAAAAAAAAAX")); // validated code
        tradeProduct.getName().add(createTextType("Eila")); // diga name
        tradeProduct.setDescription(createTextType("An Eila prescription"));

        var specifiedLineTradeAgreement = billingFactory.createLineTradeAgreementType();
        var netPrice = billingFactory.createTradePriceType();
        netPrice.getChargeAmount().add(createAmountType(new BigDecimal(100)));
        specifiedLineTradeAgreement.setNetPriceProductTradePrice(netPrice);

        var specifiedLineTradeDelivery = billingFactory.createLineTradeDeliveryType();
        specifiedLineTradeDelivery.setBilledQuantity(createQuantityType(new BigDecimal(1), "C62"));

        var specifiedLineTradeSettlement = billingFactory.createLineTradeSettlementType();
        var applicableTradeTax = billingFactory.createTradeTaxType();
        applicableTradeTax.setTypeCode(createTaxTypeCode("VAT"));
        applicableTradeTax.setCategoryCode(createTaxCategoryCode("S"));
        applicableTradeTax.setRateApplicablePercent(createPercentType(new BigDecimal(19)));
        specifiedLineTradeSettlement.getApplicableTradeTax().add(applicableTradeTax);

        var specifiedTradeSettlementLineMonetarySummation = billingFactory.createTradeSettlementLineMonetarySummationType();
        specifiedTradeSettlementLineMonetarySummation.getLineTotalAmount().add(createAmountType(new BigDecimal(100)));

        includedSupplyChainTradeLineItem.setAssociatedDocumentLineDocument(associatedDocLineDoc);
        includedSupplyChainTradeLineItem.setSpecifiedTradeProduct(tradeProduct);
        includedSupplyChainTradeLineItem.setSpecifiedLineTradeAgreement(specifiedLineTradeAgreement);
        includedSupplyChainTradeLineItem.setSpecifiedLineTradeDelivery(specifiedLineTradeDelivery);
        includedSupplyChainTradeLineItem.setSpecifiedLineTradeSettlement(specifiedLineTradeSettlement);
        // ---
        var applicableHeaderTradeAgreement = billingFactory.createHeaderTradeAgreementType();
        applicableHeaderTradeAgreement.setBuyerReference(createTextType("Leitweg-ID"));

        applicableHeaderTradeAgreement.setSellerTradeParty(
                createTradeParty(
                        DigaTradeParty.builder()
                                .companyId("TEST_RECHNUNGSSTELLER")
                                .companyName("Rechnungssteller")
                                .companyIk("987654321")
                                .taxRegistration("DE 123 456 789")
                                .contactPerson(
                                        DigaTradeParty.DigaTradePartyContactPerson.builder()
                                                .fullName("Max Mustermann")
                                                .telephoneNumber("+49 000 001 0001")
                                                .emailAddress("max.mustermann@rechnungssteller.de")
                                                .build()
                                )
                                .postalAddress(
                                        DigaTradeParty.DigaTradePartyPostalAddress.builder()
                                                .postalCode("01234")
                                                .adressLine("Musterstraße 1")
                                                .city("Berlin")
                                                .countryCode("DE")
                                        .build()
                                )
                                .build()

                )
        );
        applicableHeaderTradeAgreement.setBuyerTradeParty(
                createTradeParty(
                        DigaTradeParty.builder()
                                .companyId("TEST_RECHNUNGSEMPFÄNGER")
                                .companyIk("123456789")
                                .companyName("Rechnungsempfänger")
                                .postalAddress(
                                        DigaTradeParty.DigaTradePartyPostalAddress.builder()
                                                .postalCode("01234")
                                                .adressLine("Musterstraße 2")
                                                .city("Berlin")
                                                .countryCode("DE")
                                                .build()
                                )
                                .build()
                )
        );

        var applicableHeaderTradeDelivery = billingFactory.createHeaderTradeDeliveryType();
        var supplyChainEvent = billingFactory.createSupplyChainEventType();
        supplyChainEvent.setOccurrenceDateTime(createDateTime(new Date())); // day of service provision

        applicableHeaderTradeDelivery.setActualDeliverySupplyChainEvent(supplyChainEvent);

        var applicableHeaderTradeSettlement = billingFactory.createHeaderTradeSettlementType();

        var specifiedTradeSettlementPaymentMeans = billingFactory.createTradeSettlementPaymentMeansType();
        var paymentMeansCodeType = billingFactory.createPaymentMeansCodeType();
        paymentMeansCodeType.setValue("30"); // what is this
        specifiedTradeSettlementPaymentMeans.setTypeCode(paymentMeansCodeType);

        var settlementApplicableTradeTax = billingFactory.createTradeTaxType();
        settlementApplicableTradeTax.getCalculatedAmount().add(createAmountType(new BigDecimal(19)));
        settlementApplicableTradeTax.setTypeCode(createTaxTypeCode("VAT"));
        settlementApplicableTradeTax.getBasisAmount().add(createAmountType(new BigDecimal(100)));
        settlementApplicableTradeTax.setCategoryCode(createTaxCategoryCode("S"));
        settlementApplicableTradeTax.setRateApplicablePercent(createPercentType(new BigDecimal(19)));

        // SpecifiedTradePaymentTerms -- leaving out for now (it is blank in test bill). add if validator complains

        var specifiedTradeSettlementHeaderMonetarySummation = billingFactory.createTradeSettlementHeaderMonetarySummationType();
        specifiedTradeSettlementHeaderMonetarySummation.getLineTotalAmount().add(createAmountType(new BigDecimal(100)));
        specifiedTradeSettlementHeaderMonetarySummation.getTaxBasisTotalAmount().add(createAmountType(new BigDecimal(100)));
        specifiedTradeSettlementHeaderMonetarySummation.getTaxTotalAmount().add(createAmountType(new BigDecimal(19), "EUR"));
        specifiedTradeSettlementHeaderMonetarySummation.getGrandTotalAmount().add(createAmountType(new BigDecimal(119)));
        specifiedTradeSettlementHeaderMonetarySummation.getDuePayableAmount().add(createAmountType(new BigDecimal(119)));


        applicableHeaderTradeSettlement.setCreditorReferenceID(createIdType("987654322", "IK")); // creditor - see mapping file not same as "buyer ik"
        applicableHeaderTradeSettlement.setInvoiceCurrencyCode(createCurrencyCodeType("EUR"));
        applicableHeaderTradeSettlement.getSpecifiedTradeSettlementPaymentMeans().add(specifiedTradeSettlementPaymentMeans);
        applicableHeaderTradeSettlement.getApplicableTradeTax().add(settlementApplicableTradeTax);
        applicableHeaderTradeSettlement.setSpecifiedTradeSettlementHeaderMonetarySummation(specifiedTradeSettlementHeaderMonetarySummation);



        // after done here - try to unmarshal the bitmarck test request with our xsds and see if it validates there


        transaction.getIncludedSupplyChainTradeLineItem().add(includedSupplyChainTradeLineItem);
        transaction.setApplicableHeaderTradeAgreement(applicableHeaderTradeAgreement);
        transaction.setApplicableHeaderTradeDelivery(applicableHeaderTradeDelivery);
        transaction.setApplicableHeaderTradeSettlement(applicableHeaderTradeSettlement);
        invoice.setSupplyChainTradeTransaction(transaction);

        var bill = billingFactory.createCrossIndustryInvoice(invoice);

        try (var res = new ByteArrayOutputStream()) {
            billingMarshaller.marshal(bill, res);
            var f = new FileWriter("bill-request.xml");
            var bytes = res.toByteArray();
            f.write(IOUtils.toString(bytes, "UTF-8"));
            f.close();;
            return bytes;
        }
    }
    private DateTimeType createDateTime(Date date) {
        var localdate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        var type = billingFactory.createDateTimeType();
        var dateTimeString = billingFactory.createDateTimeTypeDateTimeString();
        dateTimeString.setFormat("102");
        var dateFormatPattern = "yyyyMMdd";

        dateTimeString.setValue(
                DateTimeFormatter.ofPattern(dateFormatPattern).format(localdate)
        );
        type.setDateTimeString(dateTimeString);
        return type;
    }
    private CurrencyCodeType createCurrencyCodeType(String value) {
        var type = billingFactory.createCurrencyCodeType();
        type.setValue(value);
        return type;
    }
    private TradePartyType createTradeParty(DigaTradeParty partyInformation) {
        var tradeParty = billingFactory.createTradePartyType();
        tradeParty.getID().add(createIdType(partyInformation.getCompanyId()));
        tradeParty.getID().add(createIdType(partyInformation.getCompanyIk(), "IK"));
        tradeParty.setName(createTextType(partyInformation.getCompanyName()));

        tradeParty.setPostalTradeAddress(
                createTradeAddressType(
                        partyInformation.getPostalAddress().getPostalCode(),
                        partyInformation.getPostalAddress().getAdressLine(),
                        partyInformation.getPostalAddress().getCity(),
                        partyInformation.getPostalAddress().getCountryCode()
                )
        );

        if (partyInformation.getContactPerson() != null) {
            var tradeContact = billingFactory.createTradeContactType();
            tradeContact.setPersonName(createTextType(partyInformation.getContactPerson().getFullName()));
            tradeContact.setTelephoneUniversalCommunication(createTelephoneCommunicationType(partyInformation.getContactPerson().getTelephoneNumber()));
            tradeContact.setEmailURIUniversalCommunication(createEmailCommunicationType(partyInformation.getContactPerson().getEmailAddress()));
            tradeParty.getDefinedTradeContact().add(tradeContact);
        }

        if (partyInformation.getTaxRegistration() != null) {
            var specifiedTaxRegistration = billingFactory.createLegalOrganizationType();
            specifiedTaxRegistration.setID(createIdType(partyInformation.getTaxRegistration(), "VA"));
            tradeParty.setSpecifiedLegalOrganization(specifiedTaxRegistration);
        }

        return tradeParty;
    }
    private TradeAddressType createTradeAddressType(String postalCode, String lineOne, String cityName, String countryId) {
        var type = billingFactory.createTradeAddressType();
        type.setPostcodeCode(createCodeType(postalCode));
        type.setLineOne(createTextType(lineOne));
        type.setCityName(createTextType(cityName));
        var countryIdType = billingFactory.createCountryIDType();
        countryIdType.setValue(countryId);
        type.setCountryID(countryIdType);
        return type;
    }
    private CodeType createCodeType(String value) {
        var type = billingFactory.createCodeType();
        type.setValue(value);
        return type;
    }
    private UniversalCommunicationType createTelephoneCommunicationType(String number) {
        var type = billingFactory.createUniversalCommunicationType();
        type.setCompleteNumber(createTextType(number));
        return type;
    }
    private UniversalCommunicationType createEmailCommunicationType(String email) {
        var type = billingFactory.createUniversalCommunicationType();
        type.setURIID(createIdType(email));
        return type;
    }
    private PercentType createPercentType(BigDecimal value) {
        var percent = billingFactory.createPercentType();
        percent.setValue(value);
        return percent;
    }
    private TaxCategoryCodeType createTaxCategoryCode(String value) {
        var code = billingFactory.createTaxCategoryCodeType();
        code.setValue(value);
        return code;
    }
    private TaxTypeCodeType createTaxTypeCode(String value) {
        var taxTypeCode = billingFactory.createTaxTypeCodeType();
        taxTypeCode.setValue(value);
        return taxTypeCode;
    }
    private QuantityType createQuantityType(BigDecimal value, String unitCode) {
        var type = billingFactory.createQuantityType();
        type.setValue(value);
        type.setUnitCode(unitCode);
        return type;
    }
    private AmountType createAmountType(BigDecimal value) {
        return createAmountType(value, null);
    }
    private AmountType createAmountType(BigDecimal value, String currencyId) {
        var type = billingFactory.createAmountType();
        type.setValue(value);
        if (currencyId != null) {
            type.setCurrencyID(currencyId);
        }
        return type;
    }
    private TextType createTextType(String text) {
        var type = billingFactory.createTextType();
        type.setValue(text);
        return type;
    }
    private IDType createIdType(String value) {
        return createIdType(value, null);
    }
    private IDType createIdType(String value, String schemeId) {
        var id = billingFactory.createIDType();
        id.setValue(value);
        if (schemeId != null) {
            id.setSchemeID(schemeId);
        }
        return id;
    }
    @Override
    public byte[] createCodeValidationRequest(DigaCodeInformation codeInformation) throws JAXBException, IOException {
        var processIdentifier = DigaUtils.isDigaTestCode(codeInformation.getFullDigaCode())
                ? VerfahrenskennungStp.TDFC_0
                : VerfahrenskennungStp.EDFC_0;


        var receiverIkWithoutPrefix = DigaUtils.ikNumberWithoutPrefix(codeInformation.getInsuranceCompanyIKNumber());
        var anfrage = objectFactory.createPruefungFreischaltcodeAnfrage();
        anfrage.setIKDiGAHersteller(senderIk);
        anfrage.setIKKrankenkasse(receiverIkWithoutPrefix);
        anfrage.setDiGAID(digaId);
        anfrage.setFreischaltcode(codeInformation.getFullDigaCode());

        var request = objectFactory.createPruefungFreischaltcode();
        request.setAnfrage(anfrage);
        request.setVerfahrenskennung(processIdentifier);
        request.setGueltigab(datatypeFactory.newXMLGregorianCalendar(DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_DATE.getValue()));
        request.setAbsender(senderIk);
        request.setEmpfaenger(receiverIkWithoutPrefix);
        request.setNachrichtentyp(NachrichtentypStp.ANF);
        request.setVersion(DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_VERSION.getValue());

        try (var res = new ByteArrayOutputStream()) {
            marshaller.marshal(request, res);
            return res.toByteArray();
        }
    }
}

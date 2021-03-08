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
import com.alextherapeutics.diga.model.*;
import com.alextherapeutics.diga.model.generatedxml.billing.*;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.NachrichtentypStp;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.ObjectFactory;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.PruefungFreischaltcode;
import com.alextherapeutics.diga.model.generatedxml.codevalidation.VerfahrenskennungStp;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * An XML writer using JAXB.
 * Depends on XML Schemas (.xsd) located in main/resources/*-xsd/
 */
@Slf4j
public class DigaXmlJaxbRequestWriter implements DigaXmlRequestWriter {
    /**
     * Static (non-changing between invoices) information about the DiGA
     * being served by this client and the manufacturing company used for creating invoices.
     */
    private DigaInformation digaInformation;

    private DatatypeFactory datatypeFactory;

    private JAXBContext codeContext;
    private Marshaller codeMarshaller;
    private com.alextherapeutics.diga.model.generatedxml.codevalidation.ObjectFactory codeObjectFactory;

    private JAXBContext billingContext;
    private Marshaller billingMarshaller;
    private com.alextherapeutics.diga.model.generatedxml.billing.ObjectFactory billingObjectFactory;

    @Builder
    public DigaXmlJaxbRequestWriter(@NonNull DigaInformation digaInformation) throws JAXBException {
        this.digaInformation = digaInformation;
        init();
    }

    @Override
    public byte[] createCodeValidationRequest(DigaCodeInformation codeInformation) throws JAXBException, IOException {
        var processIdentifier = DigaUtils.isDigaTestCode(codeInformation.getFullDigaCode())
                ? VerfahrenskennungStp.TDFC_0
                : VerfahrenskennungStp.EDFC_0;


        var receiverIkWithoutPrefix = DigaUtils.ikNumberWithoutPrefix(codeInformation.getInsuranceCompanyIKNumber());
        var anfrage = codeObjectFactory.createPruefungFreischaltcodeAnfrage();
        anfrage.setIKDiGAHersteller(DigaUtils.ikNumberWithoutPrefix(digaInformation.getManufacturingCompanyIk()));
        anfrage.setIKKrankenkasse(receiverIkWithoutPrefix);
        anfrage.setDiGAID(digaInformation.getDigaId());
        anfrage.setFreischaltcode(codeInformation.getFullDigaCode());

        var request = codeObjectFactory.createPruefungFreischaltcode();
        request.setAnfrage(anfrage);
        request.setVerfahrenskennung(processIdentifier);
        request.setGueltigab(datatypeFactory.newXMLGregorianCalendar(DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_DATE.getValue()));
        request.setAbsender(DigaUtils.ikNumberWithoutPrefix(digaInformation.getManufacturingCompanyIk()));
        request.setEmpfaenger(receiverIkWithoutPrefix);
        request.setNachrichtentyp(NachrichtentypStp.ANF);
        request.setVersion(DigaSupportedXsdVersion.DIGA_CODE_VALIDATION_VERSION.getValue());

        try (var res = new ByteArrayOutputStream()) {
            codeMarshaller.marshal(request, res);
            return res.toByteArray();
        }
    }

    @Override
    public byte[] createBillingRequest(DigaInvoice digaInvoice, DigaBillingInformation billingInformation) throws IOException, JAXBException {
        var invoice = billingObjectFactory.createCrossIndustryInvoiceType();
        invoice.setExchangedDocumentContext(createExchangedDocumentContext());
        invoice.setExchangedDocument(createExchangedDocument(digaInvoice));
        invoice.setSupplyChainTradeTransaction(createSupplyChainTradeTransaction(digaInvoice, billingInformation));
        var root = billingObjectFactory.createCrossIndustryInvoice(invoice);
        try (var res = new ByteArrayOutputStream()) {
            billingMarshaller.marshal(root, res);
            return res.toByteArray();
        }
    }

    private void init() throws JAXBException {
        datatypeFactory = DatatypeFactory.newDefaultInstance();

        codeObjectFactory = new ObjectFactory();
        codeContext = JAXBContext.newInstance(PruefungFreischaltcode.class);
        codeMarshaller = codeContext.createMarshaller();
        codeMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        billingObjectFactory = new com.alextherapeutics.diga.model.generatedxml.billing.ObjectFactory();
        billingContext = JAXBContext.newInstance(CrossIndustryInvoiceType.class);
        billingMarshaller = billingContext.createMarshaller();
        billingMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    // metadata for the document type (basically this says that it is a Xrechnung)
    private ExchangedDocumentContextType createExchangedDocumentContext() {
        var exchangedDocumentContext = billingObjectFactory.createExchangedDocumentContextType();
        var guideline = billingObjectFactory.createDocumentContextParameterType();
        var guidelineId = createIdType("urn:cen.eu:en16931:2017#compliant#urn:xoev-de:kosit:standard:xrechnung_2.0");
        guideline.setID(guidelineId);
        exchangedDocumentContext.getGuidelineSpecifiedDocumentContextParameter().add(guideline);
        return exchangedDocumentContext;
    }

    // metadata for the document
    private ExchangedDocumentType createExchangedDocument(DigaInvoice digaInvoice) {
        // ExchangedDocument
        var exchangedDocument = billingObjectFactory.createExchangedDocumentType();
        exchangedDocument.setID(createIdType(digaInvoice.getInvoiceId())); // pretty sure this is an invoice id/number, so we set it ourselves but must be unique
        var typeCode = billingObjectFactory.createDocumentCodeType();
        typeCode.setValue("380"); // should always be 380 here, means "commercial invoice"
        exchangedDocument.setTypeCode(typeCode);
        exchangedDocument.setIssueDateTime(createDateTime(digaInvoice.getIssueDate())); // issuedate, make optional in billing info otherwise today
        return exchangedDocument;
    }
    // the data of the actual transaction being invoiced
    private SupplyChainTradeTransactionType createSupplyChainTradeTransaction(DigaInvoice digaInvoice, DigaBillingInformation billingInformation) {
        var transaction = billingObjectFactory.createSupplyChainTradeTransactionType();
        transaction.getIncludedSupplyChainTradeLineItem().add(createIncludedSupplyChainTradeItem(digaInvoice));
        transaction.setApplicableHeaderTradeAgreement(createApplicableHeaderTradeAgreement(digaInvoice, billingInformation));
        transaction.setApplicableHeaderTradeDelivery(createApplicableHeaderTradeDelivery(digaInvoice));
        transaction.setApplicableHeaderTradeSettlement(createApplicableHeaderTradeSettlement(digaInvoice, billingInformation));
        return transaction;
    }

    // what has been sold (the diga id, the patient code etc) and some price/tax info on it
    private SupplyChainTradeLineItemType createIncludedSupplyChainTradeItem(DigaInvoice digaInvoice) {
        var includedSupplyChainTradeLineItem = billingObjectFactory.createSupplyChainTradeLineItemType();

        var associatedDocLineDoc = billingObjectFactory.createDocumentLineDocumentType();
        associatedDocLineDoc.setLineID(createIdType("1")); // bitmarck test says "TEST_POSITION_1",
        // CII examples say just "1"
        // it looks like it just enumerates if there are more items to sell
        // in this case theres always 1, so put "1"

        var tradeProduct = billingObjectFactory.createTradeProductType();
        tradeProduct.setGlobalID(createIdType(digaInvoice.getDigavEid()));
        tradeProduct.setBuyerAssignedID(createIdType(digaInvoice.getValidatedDigaCode(), "Freischaltcode"));
        tradeProduct.getName().add(createTextType(digaInformation.getDigaName()));
        tradeProduct.setDescription(createTextType(
                digaInformation.getDigaDescription() == null
                        ? "A " + digaInformation.getDigaName() + " prescription."
                        : digaInformation.getDigaDescription()
        ));

        var specifiedLineTradeAgreement = billingObjectFactory.createLineTradeAgreementType();
        var netPrice = billingObjectFactory.createTradePriceType();
        netPrice.getChargeAmount().add(createAmountType(digaInformation.getNetPricePerPrescription()));
        specifiedLineTradeAgreement.setNetPriceProductTradePrice(netPrice);

        var specifiedLineTradeDelivery = billingObjectFactory.createLineTradeDeliveryType();
        specifiedLineTradeDelivery.setBilledQuantity(createQuantityType(new BigDecimal(1), "C62")); // when sending diga bills this is always 1 since we send one bill for each validated code

        var specifiedLineTradeSettlement = billingObjectFactory.createLineTradeSettlementType();
        var applicableTradeTax = billingObjectFactory.createTradeTaxType();
        applicableTradeTax.setTypeCode(createTaxTypeCode("VAT")); // standard VAT. if anyone needs these values to be different, now is a good time to contribute =)
        applicableTradeTax.setCategoryCode(createTaxCategoryCode("S"));
        applicableTradeTax.setRateApplicablePercent(createPercentType(new BigDecimal(19)));
        specifiedLineTradeSettlement.getApplicableTradeTax().add(applicableTradeTax);

        var specifiedTradeSettlementLineMonetarySummation = billingObjectFactory.createTradeSettlementLineMonetarySummationType();
        specifiedTradeSettlementLineMonetarySummation.getLineTotalAmount().add(createAmountType(digaInformation.getNetPricePerPrescription())); // TODO - is it correct to set this to same as net price?
        specifiedLineTradeSettlement.setSpecifiedTradeSettlementLineMonetarySummation(specifiedTradeSettlementLineMonetarySummation);

        includedSupplyChainTradeLineItem.setAssociatedDocumentLineDocument(associatedDocLineDoc);
        includedSupplyChainTradeLineItem.setSpecifiedTradeProduct(tradeProduct);
        includedSupplyChainTradeLineItem.setSpecifiedLineTradeAgreement(specifiedLineTradeAgreement);
        includedSupplyChainTradeLineItem.setSpecifiedLineTradeDelivery(specifiedLineTradeDelivery);
        includedSupplyChainTradeLineItem.setSpecifiedLineTradeSettlement(specifiedLineTradeSettlement);
        return includedSupplyChainTradeLineItem;
    }
    // information on seller and buyer
    private HeaderTradeAgreementType createApplicableHeaderTradeAgreement(DigaInvoice digaInvoice, DigaBillingInformation billingInformation) {
        var applicableHeaderTradeAgreement = billingObjectFactory.createHeaderTradeAgreementType();
        applicableHeaderTradeAgreement.setBuyerReference(createTextType("Leitweg-ID")); // TODO what is this and how do we find it out (it is some unique routing identifier for the buyer notpresent in mapping file)
        applicableHeaderTradeAgreement.setSellerTradeParty(
                createTradeParty(
                        DigaTradeParty.builder()
                                .companyId(DigaUtils.ikNumberWithPrefix(digaInformation.getManufacturingCompanyIk()))
                                .companyName(digaInformation.getManufacturingCompanyName())
                                .companyIk(DigaUtils.ikNumberWithoutPrefix(digaInformation.getManufacturingCompanyIk()))
                                .taxRegistration(digaInformation.getManufacturingCompanyVATRegistration())
                                .contactPerson(
                                        DigaTradeParty.DigaTradePartyContactPerson.builder()
                                                .fullName(digaInformation.getContactPersonForBilling().getFullName())
                                                .telephoneNumber(digaInformation.getContactPersonForBilling().getPhoneNumber())
                                                .emailAddress(digaInformation.getContactPersonForBilling().getEmailAddress())
                                                .build()
                                )
                                .postalAddress(
                                        DigaTradeParty.DigaTradePartyPostalAddress.builder()
                                                .postalCode(digaInformation.getCompanyTradeAddress().getPostalCode())
                                                .adressLine(digaInformation.getCompanyTradeAddress().getAdressLine())
                                                .city(digaInformation.getCompanyTradeAddress().getCity())
                                                .countryCode(digaInformation.getCompanyTradeAddress().getCountryCode())
                                                .build()
                                )
                                .build()

                )
        );
        applicableHeaderTradeAgreement.setBuyerTradeParty(
                createTradeParty(
                        DigaTradeParty.builder()
                                .companyId(DigaUtils.ikNumberWithPrefix(billingInformation.getInsuranceCompanyIKNumber()))
                                .companyIk(billingInformation.getInsuranceCompanyIKNumber())
                                .companyName(billingInformation.getInsuranceCompanyName())
                                .postalAddress(
                                        DigaTradeParty.DigaTradePartyPostalAddress.builder()
                                                .postalCode(billingInformation.getBuyerCompanyPostalCode())
                                                .adressLine(billingInformation.getBuyerCompanyAddressLine())
                                                .city(billingInformation.getBuyerCompanyCity())
                                                .countryCode(billingInformation.getBuyerCompanyCountryCode())
                                                .build()
                                )
                                .build()
                )
        );
        return applicableHeaderTradeAgreement;
    }
    // time of delivery
    private HeaderTradeDeliveryType createApplicableHeaderTradeDelivery(DigaInvoice digaInvoice) {
        var applicableHeaderTradeDelivery = billingObjectFactory.createHeaderTradeDeliveryType();
        var supplyChainEvent = billingObjectFactory.createSupplyChainEventType();
        supplyChainEvent.setOccurrenceDateTime(createDateTime(digaInvoice.getDateOfServiceProvision()));
        applicableHeaderTradeDelivery.setActualDeliverySupplyChainEvent(supplyChainEvent);
        return applicableHeaderTradeDelivery;
    }

    // money details like price, taxes, etc
    private HeaderTradeSettlementType createApplicableHeaderTradeSettlement(DigaInvoice digaInvoice, DigaBillingInformation billingInformation) {
        // we calculate money values here
        var netPrice = digaInformation.getNetPricePerPrescription();
        var taxPercent = digaInformation.getApplicableVATpercent();
        var calculatedTax = taxPercent.divide(new BigDecimal(100)).multiply(netPrice);
        var grandTotal = netPrice.add(calculatedTax);


        var applicableHeaderTradeSettlement = billingObjectFactory.createHeaderTradeSettlementType();

        var specifiedTradeSettlementPaymentMeans = billingObjectFactory.createTradeSettlementPaymentMeansType();
        var paymentMeansCodeType = billingObjectFactory.createPaymentMeansCodeType();
        paymentMeansCodeType.setValue("30"); // a code from https://unece.org/fileadmin/DAM/trade/untdid/d16b/tred/tred4461.htm
        // 30 means "Credit transfer: payment by credit movement of funds from one account to another
        specifiedTradeSettlementPaymentMeans.setTypeCode(paymentMeansCodeType);


        var settlementApplicableTradeTax = billingObjectFactory.createTradeTaxType();
        settlementApplicableTradeTax.getCalculatedAmount().add(createAmountType(calculatedTax));
        settlementApplicableTradeTax.setTypeCode(createTaxTypeCode("VAT"));
        settlementApplicableTradeTax.getBasisAmount().add(createAmountType(netPrice));
        settlementApplicableTradeTax.setCategoryCode(createTaxCategoryCode("S"));
        settlementApplicableTradeTax.setRateApplicablePercent(createPercentType(taxPercent));

        // bitmarck diga validator fails if this is not empty. we are not allowed a due date or a description, it must have description with emptytext
        var specifiedTradePaymentTerms = billingObjectFactory.createTradePaymentTermsType();
        specifiedTradePaymentTerms.getDescription().add(createTextType(""));

        var specifiedTradeSettlementHeaderMonetarySummation = billingObjectFactory.createTradeSettlementHeaderMonetarySummationType();
        specifiedTradeSettlementHeaderMonetarySummation.getLineTotalAmount().add(createAmountType(netPrice));
        specifiedTradeSettlementHeaderMonetarySummation.getTaxBasisTotalAmount().add(createAmountType(netPrice));
        specifiedTradeSettlementHeaderMonetarySummation.getTaxTotalAmount().add(createAmountType(taxPercent, digaInvoice.getInvoiceCurrencyCode()));
        specifiedTradeSettlementHeaderMonetarySummation.getGrandTotalAmount().add(createAmountType(grandTotal));
        specifiedTradeSettlementHeaderMonetarySummation.getDuePayableAmount().add(createAmountType(grandTotal));

        applicableHeaderTradeSettlement.setCreditorReferenceID(createIdType(billingInformation.getBuyerCompanyCreditorIk(), "IK")); // creditor - see mapping file not same as "buyer ik"
        applicableHeaderTradeSettlement.setInvoiceCurrencyCode(createCurrencyCodeType(digaInvoice.getInvoiceCurrencyCode()));
        applicableHeaderTradeSettlement.getSpecifiedTradeSettlementPaymentMeans().add(specifiedTradeSettlementPaymentMeans);
        applicableHeaderTradeSettlement.getApplicableTradeTax().add(settlementApplicableTradeTax);
        applicableHeaderTradeSettlement.getSpecifiedTradePaymentTerms().add(specifiedTradePaymentTerms);
        applicableHeaderTradeSettlement.setSpecifiedTradeSettlementHeaderMonetarySummation(specifiedTradeSettlementHeaderMonetarySummation);

        return applicableHeaderTradeSettlement;
    }
    private DateTimeType createDateTime(Date date) {
        var localdate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        var type = billingObjectFactory.createDateTimeType();
        var dateTimeString = billingObjectFactory.createDateTimeTypeDateTimeString();
        dateTimeString.setFormat("102");
        var dateFormatPattern = "yyyyMMdd";

        dateTimeString.setValue(
                DateTimeFormatter.ofPattern(dateFormatPattern).format(localdate)
        );
        type.setDateTimeString(dateTimeString);
        return type;
    }
    private CurrencyCodeType createCurrencyCodeType(String value) {
        var type = billingObjectFactory.createCurrencyCodeType();
        type.setValue(value);
        return type;
    }
    private TradePartyType createTradeParty(DigaTradeParty partyInformation) {
        var tradeParty = billingObjectFactory.createTradePartyType();
        tradeParty.getID().add(createIdType(partyInformation.getCompanyId()));
        tradeParty.getID().add(createIdType(partyInformation.getCompanyIk(), "IK"));
        tradeParty.setName(createTextType(partyInformation.getCompanyName()));

        if (partyInformation.getPostalAddress() != null) {
            tradeParty.setPostalTradeAddress(
                    createTradeAddressType(
                            partyInformation.getPostalAddress().getPostalCode(),
                            partyInformation.getPostalAddress().getAdressLine(),
                            partyInformation.getPostalAddress().getCity(),
                            partyInformation.getPostalAddress().getCountryCode()
                    )
            );
        }

        if (partyInformation.getContactPerson() != null) {
            var tradeContact = billingObjectFactory.createTradeContactType();
            tradeContact.setPersonName(createTextType(partyInformation.getContactPerson().getFullName()));
            tradeContact.setTelephoneUniversalCommunication(createTelephoneCommunicationType(partyInformation.getContactPerson().getTelephoneNumber()));
            tradeContact.setEmailURIUniversalCommunication(createEmailCommunicationType(partyInformation.getContactPerson().getEmailAddress()));
            tradeParty.getDefinedTradeContact().add(tradeContact);
        }

        if (partyInformation.getTaxRegistration() != null) {
            var specifiedTaxRegistration = billingObjectFactory.createTaxRegistrationType();
            specifiedTaxRegistration.setID(createIdType(partyInformation.getTaxRegistration(), "VA"));
            tradeParty.getSpecifiedTaxRegistration().add(specifiedTaxRegistration);
        }

        return tradeParty;
    }
    private TradeAddressType createTradeAddressType(String postalCode, String lineOne, String cityName, String countryId) {
        var type = billingObjectFactory.createTradeAddressType();
        type.setPostcodeCode(createCodeType(postalCode));
        type.setLineOne(createTextType(lineOne));
        type.setCityName(createTextType(cityName));
        var countryIdType = billingObjectFactory.createCountryIDType();
        countryIdType.setValue(countryId);
        type.setCountryID(countryIdType);
        return type;
    }
    private CodeType createCodeType(String value) {
        var type = billingObjectFactory.createCodeType();
        type.setValue(value);
        return type;
    }
    private UniversalCommunicationType createTelephoneCommunicationType(String number) {
        var type = billingObjectFactory.createUniversalCommunicationType();
        type.setCompleteNumber(createTextType(number));
        return type;
    }
    private UniversalCommunicationType createEmailCommunicationType(String email) {
        var type = billingObjectFactory.createUniversalCommunicationType();
        type.setURIID(createIdType(email));
        return type;
    }
    private PercentType createPercentType(BigDecimal value) {
        var percent = billingObjectFactory.createPercentType();
        percent.setValue(value);
        return percent;
    }
    private TaxCategoryCodeType createTaxCategoryCode(String value) {
        var code = billingObjectFactory.createTaxCategoryCodeType();
        code.setValue(value);
        return code;
    }
    private TaxTypeCodeType createTaxTypeCode(String value) {
        var taxTypeCode = billingObjectFactory.createTaxTypeCodeType();
        taxTypeCode.setValue(value);
        return taxTypeCode;
    }
    private QuantityType createQuantityType(BigDecimal value, String unitCode) {
        var type = billingObjectFactory.createQuantityType();
        type.setValue(value);
        type.setUnitCode(unitCode);
        return type;
    }
    private AmountType createAmountType(BigDecimal value) {
        return createAmountType(value, null);
    }
    private AmountType createAmountType(BigDecimal value, String currencyId) {
        var type = billingObjectFactory.createAmountType();
        type.setValue(value);
        if (currencyId != null) {
            type.setCurrencyID(currencyId);
        }
        return type;
    }
    private TextType createTextType(String text) {
        var type = billingObjectFactory.createTextType();
        type.setValue(text);
        return type;
    }
    private IDType createIdType(String value) {
        return createIdType(value, null);
    }
    private IDType createIdType(String value, String schemeId) {
        var id = billingObjectFactory.createIDType();
        id.setValue(value);
        if (schemeId != null) {
            id.setSchemeID(schemeId);
        }
        return id;
    }
    @Builder
    @Getter
    static class DigaTradeParty {
        @NonNull
        private String companyId;
        @NonNull
        private String companyIk;
        @NonNull
        private String companyName;
        private String taxRegistration;
        private DigaTradeParty.DigaTradePartyContactPerson contactPerson;
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
}

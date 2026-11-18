package com.grab.service.contract.message.merchandising.pricing;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Universal Java Port Interface for Price Sets, Variant Price Links, and Preferences.
 * Replaces PriceSetController, VariantPriceSetLinkController, and PricePreferenceController.
 */
public interface PriceSetPort extends Remote {

    PriceSetMessageResult createPriceSet() throws RemoteException;

    PriceSetMessageResult findPriceSetById(Id priceSetId) throws RemoteException;

    List<CalculatedPriceMessageResult> calculatePrices(CalculatePriceMessage message) throws RemoteException;

    PricePreferenceMessageResult createPricePreference(CreatePricePreferenceMessage message) throws RemoteException;

    PricePreferenceMessageResult findPricePreferenceById(Id preferenceId) throws RemoteException;

    record CalculatePriceMessage(
            List<Id> priceSetIds,
            String currencyCode,
            Integer quantity,
            Map<String, String> attributes
    ) implements Serializable {}

    record CalculatedPriceMessageResult(
            String id,
            String currencyCode,
            BigDecimal calculatedAmount,
            boolean calculatedPricePriceList,
            boolean calculatedPriceTaxInclusive,
            BigDecimal originalAmount,
            boolean originalPricePriceList,
            boolean originalPriceTaxInclusive
    ) implements Serializable {}

    record PriceSetMessageResult(
            String id,
            List<PriceEntryMessageResult> prices
    ) implements Serializable {}

    record PriceEntryMessageResult(
            String id,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            String priceSetId,
            String priceListId
    ) implements Serializable {}

    record CreatePricePreferenceMessage(
            String attribute,
            String value,
            boolean taxInclusive
    ) implements Serializable {}

    record PricePreferenceMessageResult(
            String id,
            String attribute,
            String value,
            boolean taxInclusive
    ) implements Serializable {}
}

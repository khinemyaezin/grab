package com.grab.app.merchandising.pricing.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Java Port Interface for Price Sets, Variant Price Links, and Preferences.
 * Replaces PriceSetController, VariantPriceSetLinkController, and PricePreferenceController.
 */
public interface PriceSetPort {

    PriceSetMessageResult createPriceSet();

    Optional<PriceSetMessageResult> findPriceSetById(Id priceSetId);

    List<CalculatedPriceMessageResult> calculatePrices(CalculatePriceMessage message);

    PricePreferenceMessageResult createPricePreference(CreatePricePreferenceMessage message);

    Optional<PricePreferenceMessageResult> findPricePreferenceById(Id preferenceId);

    record CalculatePriceMessage(
            List<Id> priceSetIds,
            String currencyCode,
            Integer quantity,
            Map<String, String> attributes
    ) {}

    record CalculatedPriceMessageResult(
            String id,
            String currencyCode,
            BigDecimal calculatedAmount,
            boolean calculatedPricePriceList,
            boolean calculatedPriceTaxInclusive,
            BigDecimal originalAmount,
            boolean originalPricePriceList,
            boolean originalPriceTaxInclusive
    ) {}

    record PriceSetMessageResult(
            String id,
            List<PriceEntryMessageResult> prices
    ) {}

    record PriceEntryMessageResult(
            String id,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            String priceSetId,
            String priceListId
    ) {}

    record CreatePricePreferenceMessage(
            String attribute,
            String value,
            boolean taxInclusive
    ) {}

    record PricePreferenceMessageResult(
            String id,
            String attribute,
            String value,
            boolean taxInclusive
    ) {}
}

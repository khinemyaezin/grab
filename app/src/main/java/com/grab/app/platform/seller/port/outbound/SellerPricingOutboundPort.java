package com.grab.app.platform.seller.port.outbound;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Seller Outbound Port for Pricing, Price Sets, and Preferences.
 */
public interface SellerPricingOutboundPort {

    PriceListOutboundResult createPriceList(CreatePriceListRecord record);

    Optional<PriceListOutboundResult> findPriceListById(String priceListId);

    List<PriceListOutboundResult> listPriceLists();

    PriceSetOutboundResult createPriceSet();

    Optional<PriceSetOutboundResult> findPriceSetById(String priceSetId);

    List<CalculatedPriceOutboundResult> calculatePrices(CalculatePriceRecord record);

    PricePreferenceOutboundResult createPricePreference(CreatePricePreferenceRecord record);

    Optional<PricePreferenceOutboundResult> findPricePreference(String preferenceId);

    record CreatePriceListRecord(
            String title,
            String description,
            String type,
            Instant startsAt,
            Instant endsAt
    ) {}

    record CalculatePriceRecord(
            List<String> priceSetIds,
            String currencyCode,
            Integer quantity,
            Map<String, String> attributes
    ) {}

    record CreatePricePreferenceRecord(
            String attribute,
            String value,
            boolean taxInclusive
    ) {}

    record PriceListOutboundResult(
            String id,
            String title,
            String description,
            String status,
            String type,
            Instant startsAt,
            Instant endsAt,
            List<PriceListRuleOutboundResult> rules,
            List<PriceEntryOutboundResult> prices
    ) {}

    record PriceListRuleOutboundResult(
            String id,
            String attribute,
            List<String> values
    ) {}

    record PriceEntryOutboundResult(
            String id,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            String priceSetId,
            String priceListId
    ) {}

    record PriceSetOutboundResult(
            String id,
            List<PriceEntryOutboundResult> prices
    ) {}

    record CalculatedPriceOutboundResult(
            String id,
            String currencyCode,
            BigDecimal calculatedAmount,
            boolean calculatedPricePriceList,
            boolean calculatedPriceTaxInclusive,
            BigDecimal originalAmount,
            boolean originalPricePriceList,
            boolean originalPriceTaxInclusive
    ) {}

    record PricePreferenceOutboundResult(
            String id,
            String attribute,
            String value,
            boolean taxInclusive
    ) {}
}

package com.grab.app.merchandising.pricing.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Price Lists and Pricing Rules.
 * Replaces PriceListController and PricingAttributeKeysController.
 */
public interface PriceListPort {

    PriceListResult createPriceList(CreatePriceListMessage message);

    PriceListResult updatePriceList(Id priceListId, UpdatePriceListMessage message);

    void deletePriceList(Id priceListId);

    void updateRules(Id priceListId, List<PriceRuleMessage> rules);

    void addPrices(Id priceListId, List<AddPriceEntryMessage> prices);

    void deletePrice(Id priceListId, Id priceId);

    Optional<PriceListResult> findPriceListById(Id priceListId);

    List<PriceListResult> listPriceLists(Id merchantId);

    List<String> listPricingAttributeKeys();

    record CreatePriceListMessage(
            Id merchantId,
            String name,
            String description,
            String currencyCode,
            String status,
            Instant startsAt,
            Instant endsAt,
            List<PriceRuleMessage> rules
    ) {}

    record UpdatePriceListMessage(
            String name,
            String description,
            String status,
            Instant startsAt,
            Instant endsAt
    ) {}

    record PriceRuleMessage(
            String attributeKey,
            String operator,
            String value
    ) {}

    record AddPriceEntryMessage(
            Id variantId,
            BigDecimal amount,
            String currencyCode,
            int minQuantity
    ) {}

    record PriceListResult(
            Id id,
            Id merchantId,
            String name,
            String description,
            String currencyCode,
            String status,
            Instant startsAt,
            Instant endsAt,
            List<PriceRuleMessage> rules,
            Instant createdAt
    ) {}
}


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

    PriceListMessageResult createPriceList(CreatePriceListMessage message);

    Optional<PriceListMessageResult> findPriceListById(Id priceListId);

    List<PriceListMessageResult> listPriceLists();

    record CreatePriceListMessage(
            String title,
            String description,
            String type,
            Instant startsAt,
            Instant endsAt
    ) {}

    record PriceListMessageResult(
            String id,
            String title,
            String description,
            String status,
            String type,
            Instant startsAt,
            Instant endsAt,
            List<PriceListRuleMessageResult> rules,
            List<PriceEntryMessageResult> prices
    ) {}

    record PriceListRuleMessageResult(
            String id,
            String attribute,
            List<String> values
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
}

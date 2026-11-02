package com.grab.app.merchandising.pricing.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Price Sets, Variant Price Links, and Preferences.
 * Replaces PriceSetController, VariantPriceSetLinkController, and PricePreferenceController.
 */
public interface PriceSetPort {

    PriceSetResult createPriceSet(CreatePriceSetMessage message);

    void addPrices(Id priceSetId, List<PriceValueMessage> prices);

    void updatePrice(Id priceSetId, Id priceId, UpdatePriceValueMessage message);

    void deletePrice(Id priceSetId, Id priceId);

    void deletePriceSet(Id priceSetId);

    Optional<PriceSetResult> findPriceSetById(Id priceSetId);

    void linkVariantToPriceSet(Id variantId, Id priceSetId);

    Optional<Id> findPriceSetIdForVariant(Id variantId);

    CalculatedPriceResult calculatePrice(CalculatePriceMessage message);

    // Preferences
    PricePreferenceResult createPricePreference(CreatePricePreferenceMessage message);

    Optional<PricePreferenceResult> findPricePreferenceById(Id preferenceId);

    void deletePricePreference(Id preferenceId);

    record CreatePriceSetMessage(
            Id merchantId,
            String name,
            List<PriceValueMessage> initialPrices
    ) {}

    record PriceValueMessage(
            BigDecimal amount,
            String currencyCode,
            Id regionId,
            int minQuantity
    ) {}

    record UpdatePriceValueMessage(
            BigDecimal amount,
            int minQuantity
    ) {}

    record CalculatePriceMessage(
            Id priceSetId,
            String currencyCode,
            Id regionId,
            int quantity
    ) {}

    record CalculatedPriceResult(
            BigDecimal calculatedAmount,
            BigDecimal originalAmount,
            String currencyCode,
            Id appliedPriceSetId
    ) {}

    record PriceSetResult(
            Id id,
            Id merchantId,
            String name,
            List<PriceEntryResult> prices,
            Instant createdAt
    ) {}

    record PriceEntryResult(
            Id id,
            BigDecimal amount,
            String currencyCode,
            Id regionId,
            int minQuantity
    ) {}

    record CreatePricePreferenceMessage(
            Id merchantId,
            String scopeKey,
            String scopeId,
            String rule
    ) {}

    record PricePreferenceResult(
            Id id,
            Id merchantId,
            String scopeKey,
            String scopeId,
            String rule
    ) {}
}


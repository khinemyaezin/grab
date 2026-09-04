package com.pricing.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.pricing.domain.entity.Price;
import com.pricing.domain.entity.PriceRule;
import com.pricing.domain.enums.PriceRuleOperator;
import com.pricing.domain.exception.PricingDomainException;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceSetTest {

    private final Instant now = Instant.parse("2026-09-01T00:00:00Z");
    private final Instant later = Instant.parse("2026-09-01T01:00:00Z");

    @Test
    void applyBasePrice_whenNoMatch_shouldAddPrice() {
        PriceSet priceSet = PriceSet.create(new CommonId("set-1"), now);
        Price candidate = basePrice(priceSet, "price-1", "usd", "10.00", null, null);

        Price applied = priceSet.applyBasePrice(candidate, later);

        assertThat(applied.getId().getValue()).isEqualTo("price-1");
        assertThat(priceSet.getPrices()).hasSize(1);
        assertThat(priceSet.getPrices().getFirst().getAmount().value()).isEqualByComparingTo("10.00");
        assertThat(priceSet.getUpdatedAt()).isEqualTo(later);
    }

    @Test
    void applyBasePrice_whenMatchingCurrencyAndQuantity_shouldReplaceDetailsKeepingId() {
        PriceSet priceSet = PriceSet.create(new CommonId("set-1"), now);
        Price original = basePrice(priceSet, "price-1", "usd", "10.00", null, null);
        priceSet.addPrice(original, now);
        PriceRule nextRule = new PriceRule(
                new CommonId("rule-1"),
                "region",
                "mm",
                PriceRuleOperator.EQ,
                1
        );
        Price candidate = Price.createBase(
                new CommonId("price-new"),
                priceSet.getId(),
                "Updated",
                CurrencyCode.of("usd"),
                MoneyAmount.of(new BigDecimal("12.50")),
                null,
                null,
                List.of(nextRule)
        );

        Price applied = priceSet.applyBasePrice(candidate, later);

        assertThat(applied.getId().getValue()).isEqualTo("price-1");
        assertThat(priceSet.getPrices()).hasSize(1);
        Price stored = priceSet.getPrices().getFirst();
        assertThat(stored.getId().getValue()).isEqualTo("price-1");
        assertThat(stored.getTitle()).isEqualTo("Updated");
        assertThat(stored.getAmount().value()).isEqualByComparingTo("12.50");
        assertThat(stored.getRules()).hasSize(1);
        assertThat(stored.getRules().getFirst().getAttribute()).isEqualTo("region");
    }

    @Test
    void applyBasePrice_whenSameCurrencyDifferentQuantityBand_shouldAddPrice() {
        PriceSet priceSet = PriceSet.create(new CommonId("set-1"), now);
        Price lowTier = basePrice(priceSet, "price-low", "usd", "10.00", 1, 4);
        priceSet.addPrice(lowTier, now);
        Price highTier = basePrice(priceSet, "price-high", "usd", "8.00", 5, 10);

        priceSet.applyBasePrice(highTier, later);

        assertThat(priceSet.getPrices()).hasSize(2);
        Optional<Price> matchingHigh = priceSet.findMatchingBasePrice(CurrencyCode.of("usd"), 5, 10);
        assertThat(matchingHigh).isPresent();
        assertThat(matchingHigh.get().getId().getValue()).isEqualTo("price-high");
        assertThat(priceSet.findMatchingBasePrice(CurrencyCode.of("usd"), 1, 4)).isPresent();
    }

    @Test
    void findMatchingBasePrice_shouldIgnoreCampaignPrice() {
        CommonId setId = new CommonId("set-1");
        Price campaign = Price.createCampaign(
                new CommonId("campaign-1"),
                setId,
                new CommonId("list-1"),
                "Sale",
                CurrencyCode.of("usd"),
                MoneyAmount.of(new BigDecimal("7.00")),
                null,
                null,
                List.of()
        );
        PriceSet priceSet = new PriceSet(setId, List.of(campaign), now, now, 0);

        Optional<Price> matching = priceSet.findMatchingBasePrice(CurrencyCode.of("usd"), null, null);

        assertThat(matching).isEmpty();
    }

    @Test
    void applyBasePrice_whenCampaignExistsForCurrency_shouldAddBasePrice() {
        CommonId setId = new CommonId("set-1");
        Price campaign = Price.createCampaign(
                new CommonId("campaign-1"),
                setId,
                new CommonId("list-1"),
                "Sale",
                CurrencyCode.of("usd"),
                MoneyAmount.of(new BigDecimal("7.00")),
                null,
                null,
                List.of()
        );
        PriceSet priceSet = new PriceSet(setId, List.of(campaign), now, now, 0);
        Price candidate = basePrice(priceSet, "price-1", "usd", "10.00", null, null);

        Price applied = priceSet.applyBasePrice(candidate, later);

        assertThat(applied.getId().getValue()).isEqualTo("price-1");
        assertThat(priceSet.getPrices()).hasSize(2);
        assertThat(priceSet.findMatchingBasePrice(CurrencyCode.of("usd"), null, null)).isPresent();
    }

    @Test
    void applyBasePrice_whenCampaignCandidate_shouldThrow() {
        PriceSet priceSet = PriceSet.create(new CommonId("set-1"), now);
        Price campaign = Price.createCampaign(
                new CommonId("campaign-1"),
                priceSet.getId(),
                new CommonId("list-1"),
                "Sale",
                CurrencyCode.of("usd"),
                MoneyAmount.of(new BigDecimal("7.00")),
                null,
                null,
                List.of()
        );

        assertThatThrownBy(() -> priceSet.applyBasePrice(campaign, later))
                .isInstanceOf(PricingDomainException.class)
                .hasMessageContaining("base price");
    }

    private Price basePrice(
            PriceSet priceSet,
            String priceId,
            String currency,
            String amount,
            Integer minQuantity,
            Integer maxQuantity
    ) {
        return Price.createBase(
                new CommonId(priceId),
                priceSet.getId(),
                "Base",
                CurrencyCode.of(currency),
                MoneyAmount.of(new BigDecimal(amount)),
                minQuantity,
                maxQuantity,
                List.of()
        );
    }
}

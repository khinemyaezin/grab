package com.pricing.domain.policy;

import com.grab.framework.id.impl.CommonId;
import com.pricing.domain.enums.PriceListStatus;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.enums.PriceRuleOperator;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import com.pricing.domain.valueobject.PricingContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CalculatePricesPolicyTest {

    private final CalculatePricesPolicy policy = new CalculatePricesPolicy();
    private final Instant now = Instant.parse("2026-07-01T12:00:00Z");

    @Test
    void calculate_withVolumeTier_shouldSelectMatchingQuantityBand() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate lowTier = baseCandidate(
                "price-low",
                priceSetId,
                "10.00",
                1,
                4,
                List.of()
        );
        PriceCandidate highTier = baseCandidate(
                "price-high",
                priceSetId,
                "8.00",
                5,
                10,
                List.of()
        );
        PricingContext context = new PricingContext(
                CurrencyCode.of("usd"),
                6,
                Map.of()
        );

        List<CalculatedPriceSet> results = policy.calculate(
                List.of(priceSetId),
                context,
                List.of(lowTier, highTier),
                List.of(),
                now
        );

        assertThat(results).hasSize(1);
        CalculatedPriceSet result = results.getFirst();
        assertThat(result.calculatedAmount()).isEqualByComparingTo("8.00");
        assertThat(result.calculatedPrice().priceId().getValue()).isEqualTo("price-high");
    }

    @Test
    void calculate_withOverride_shouldReplaceBaseRegardlessOfAmount() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate base = baseCandidate("price-base", priceSetId, "100.00", null, null, List.of());
        PriceCandidate override = listCandidate(
                "price-override",
                priceSetId,
                "plist-override",
                PriceListType.OVERRIDE,
                "120.00",
                List.of()
        );
        PricingContext context = new PricingContext(CurrencyCode.of("usd"), 1, Map.of());

        List<CalculatedPriceSet> results = policy.calculate(
                List.of(priceSetId),
                context,
                List.of(base, override),
                List.of(),
                now
        );

        CalculatedPriceSet result = results.getFirst();
        assertThat(result.calculatedAmount()).isEqualByComparingTo("120.00");
        assertThat(result.calculatedPrice().priceListType()).isEqualTo(PriceListType.OVERRIDE);
        assertThat(result.originalAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    void calculate_withSaleAndOverrideSameSpecificity_shouldRankByLowestAmount() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate base = baseCandidate("price-base", priceSetId, "100.00", null, null, List.of());
        PriceCandidate sale = listCandidate(
                "price-sale",
                priceSetId,
                "plist-sale",
                PriceListType.SALE,
                "70.00",
                List.of()
        );
        PriceCandidate override = listCandidate(
                "price-override",
                priceSetId,
                "plist-override",
                PriceListType.OVERRIDE,
                "120.00",
                List.of()
        );
        PricingContext context = new PricingContext(CurrencyCode.of("usd"), 1, Map.of());

        CalculatedPriceSet result = policy.calculate(
                List.of(priceSetId),
                context,
                List.of(base, sale, override),
                List.of(),
                now
        ).getFirst();

        assertThat(result.calculatedAmount()).isEqualByComparingTo("70.00");
        assertThat(result.calculatedPrice().priceListType()).isEqualTo(PriceListType.SALE);
    }

    @Test
    void calculate_withSaleCheaperThanBase_shouldUseSaleAsCalculated() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate base = baseCandidate("price-base", priceSetId, "100.00", null, null, List.of());
        PriceCandidate sale = listCandidate(
                "price-sale",
                priceSetId,
                "plist-sale",
                PriceListType.SALE,
                "70.00",
                List.of()
        );
        PricingContext context = new PricingContext(CurrencyCode.of("usd"), 1, Map.of());

        CalculatedPriceSet result = policy.calculate(
                List.of(priceSetId),
                context,
                List.of(base, sale),
                List.of(),
                now
        ).getFirst();

        assertThat(result.calculatedAmount()).isEqualByComparingTo("70.00");
        assertThat(result.calculatedPricePriceList()).isTrue();
        assertThat(result.originalAmount()).isEqualByComparingTo("100.00");
        assertThat(result.originalPricePriceList()).isFalse();
    }

    @Test
    void calculate_withContextRules_shouldRequireFullRuleMatch() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate base = baseCandidate("price-base", priceSetId, "50.00", null, null, List.of());
        PriceCandidate regional = baseCandidate(
                "price-region",
                priceSetId,
                "40.00",
                null,
                null,
                List.of(new PriceCandidate.RuleCondition(PricingAttributeKeys.REGION_ID, "reg_1", PriceRuleOperator.EQ))
        );
        PricingContext matching = new PricingContext(
                CurrencyCode.of("usd"),
                1,
                Map.of(PricingAttributeKeys.REGION_ID, "reg_1")
        );
        PricingContext mismatching = new PricingContext(
                CurrencyCode.of("usd"),
                1,
                Map.of(PricingAttributeKeys.REGION_ID, "reg_2")
        );

        CalculatedPriceSet matched = policy.calculate(
                List.of(priceSetId),
                matching,
                List.of(base, regional),
                List.of(),
                now
        ).getFirst();
        CalculatedPriceSet unmatched = policy.calculate(
                List.of(priceSetId),
                mismatching,
                List.of(base, regional),
                List.of(),
                now
        ).getFirst();

        assertThat(matched.calculatedAmount()).isEqualByComparingTo("40.00");
        assertThat(unmatched.calculatedAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void calculate_withInactiveOrExpiredPriceList_shouldIgnoreCampaignPrice() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate base = baseCandidate("price-base", priceSetId, "50.00", null, null, List.of());
        PriceCandidate inactive = new PriceCandidate(
                new CommonId("price-inactive"),
                priceSetId,
                new CommonId("plist-inactive"),
                PriceListType.SALE,
                PriceListStatus.DRAFT,
                null,
                null,
                "usd",
                new BigDecimal("20.00"),
                null,
                null,
                0,
                0,
                List.of(),
                List.of()
        );
        PriceCandidate expired = new PriceCandidate(
                new CommonId("price-expired"),
                priceSetId,
                new CommonId("plist-expired"),
                PriceListType.SALE,
                PriceListStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z"),
                "usd",
                new BigDecimal("15.00"),
                null,
                null,
                0,
                0,
                List.of(),
                List.of()
        );
        PricingContext context = new PricingContext(CurrencyCode.of("usd"), 1, Map.of());

        CalculatedPriceSet result = policy.calculate(
                List.of(priceSetId),
                context,
                List.of(base, inactive, expired),
                List.of(),
                now
        ).getFirst();

        assertThat(result.calculatedAmount()).isEqualByComparingTo("50.00");
        assertThat(result.calculatedPricePriceList()).isFalse();
    }

    @Test
    void calculate_withCustomerGroupListRule_shouldMatchOnlyTargetedGroups() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate base = baseCandidate("price-base", priceSetId, "80.00", null, null, List.of());
        PriceCandidate vip = new PriceCandidate(
                new CommonId("price-vip"),
                priceSetId,
                new CommonId("plist-vip"),
                PriceListType.SALE,
                PriceListStatus.ACTIVE,
                null,
                null,
                "usd",
                new BigDecimal("60.00"),
                null,
                null,
                0,
                1,
                List.of(),
                List.of(new PriceCandidate.ListRuleCondition(PricingAttributeKeys.CUSTOMER_GROUP_ID, List.of("cg_vip")))
        );
        PricingContext vipContext = new PricingContext(
                CurrencyCode.of("usd"),
                1,
                Map.of(PricingAttributeKeys.CUSTOMER_GROUP_ID, "cg_vip")
        );
        PricingContext regularContext = new PricingContext(
                CurrencyCode.of("usd"),
                1,
                Map.of(PricingAttributeKeys.CUSTOMER_GROUP_ID, "cg_regular")
        );

        CalculatedPriceSet vipResult = policy.calculate(
                List.of(priceSetId),
                vipContext,
                List.of(base, vip),
                List.of(),
                now
        ).getFirst();
        CalculatedPriceSet regularResult = policy.calculate(
                List.of(priceSetId),
                regularContext,
                List.of(base, vip),
                List.of(),
                now
        ).getFirst();

        assertThat(vipResult.calculatedAmount()).isEqualByComparingTo("60.00");
        assertThat(regularResult.calculatedAmount()).isEqualByComparingTo("80.00");
    }

    @Test
    void calculate_withRegionTaxPreference_shouldMarkAmountsTaxInclusive() {
        CommonId priceSetId = new CommonId("set-1");
        PriceCandidate base = baseCandidate("price-base", priceSetId, "50.00", null, null, List.of());
        PricingContext context = new PricingContext(
                CurrencyCode.of("usd"),
                1,
                Map.of(PricingAttributeKeys.REGION_ID, "reg_1")
        );
        PricePreferenceView preference = new PricePreferenceView(
                new CommonId("pref-1"),
                PricingAttributeKeys.REGION_ID,
                "reg_1",
                true
        );

        CalculatedPriceSet result = policy.calculate(
                List.of(priceSetId),
                context,
                List.of(base),
                List.of(preference),
                now
        ).getFirst();

        assertThat(result.calculatedPriceTaxInclusive()).isTrue();
        assertThat(result.originalPriceTaxInclusive()).isTrue();
    }

    private PriceCandidate baseCandidate(
            String priceId,
            CommonId priceSetId,
            String amount,
            Integer minQuantity,
            Integer maxQuantity,
            List<PriceCandidate.RuleCondition> rules
    ) {
        return new PriceCandidate(
                new CommonId(priceId),
                priceSetId,
                null,
                null,
                null,
                null,
                null,
                "usd",
                new BigDecimal(amount),
                minQuantity,
                maxQuantity,
                rules.size(),
                0,
                rules,
                List.of()
        );
    }

    private PriceCandidate listCandidate(
            String priceId,
            CommonId priceSetId,
            String priceListId,
            PriceListType type,
            String amount,
            List<PriceCandidate.ListRuleCondition> listRules
    ) {
        return new PriceCandidate(
                new CommonId(priceId),
                priceSetId,
                new CommonId(priceListId),
                type,
                PriceListStatus.ACTIVE,
                null,
                null,
                "usd",
                new BigDecimal(amount),
                null,
                null,
                0,
                listRules.size(),
                List.of(),
                listRules
        );
    }
}

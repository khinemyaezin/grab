package com.grab.store.pricing.internal.util;

import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.aggregate.PricePreference;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.entity.Price;
import com.pricing.domain.entity.PriceListRule;
import com.pricing.domain.entity.PriceRule;
import com.pricing.domain.enums.PriceRuleOperator;
import com.pricing.domain.policy.CalculatedPriceSet;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.command.PriceRuleInput;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.query.CalculatedPriceSetResult;

import java.util.List;

public final class PricingResultMapper {
    private PricingResultMapper() {
    }

    public static PriceSetResult toPriceSetResult(PriceSet priceSet) {
        return new PriceSetResult(
                priceSet.getId().getValue(),
                priceSet.getPrices().stream().map(PricingResultMapper::toPriceSetPrice).toList()
        );
    }

    public static PriceListResult toPriceListResult(PriceList priceList) {
        return new PriceListResult(
                priceList.getId().getValue(),
                priceList.getTitle(),
                priceList.getDescription(),
                priceList.getStatus().name(),
                priceList.getType().name(),
                priceList.getStartsAt(),
                priceList.getEndsAt(),
                priceList.getRules().stream().map(PricingResultMapper::toListRule).toList(),
                priceList.getPrices().stream().map(PricingResultMapper::toPriceListPrice).toList()
        );
    }

    public static PricePreferenceResult toPreferenceResult(PricePreference preference) {
        return new PricePreferenceResult(
                preference.getId().getValue(),
                preference.getAttribute(),
                preference.getValue(),
                preference.isTaxInclusive()
        );
    }

    public static CalculatedPriceSetResult toCalculatedResult(CalculatedPriceSet calculated) {
        return new CalculatedPriceSetResult(
                calculated.priceSetId().getValue(),
                calculated.currencyCode(),
                calculated.calculatedAmount(),
                calculated.calculatedPricePriceList(),
                calculated.calculatedPriceTaxInclusive(),
                toSelected(calculated.calculatedPrice()),
                calculated.originalAmount(),
                calculated.originalPricePriceList(),
                calculated.originalPriceTaxInclusive(),
                toSelected(calculated.originalPrice())
        );
    }

    public static List<PriceRule> toRules(IdGenerator idGenerator, List<PriceRuleInput> inputs) {
        if (inputs == null) {
            return List.of();
        }
        return inputs.stream().map(input -> new PriceRule(
                idGenerator.generateId(),
                input.attribute(),
                input.value(),
                parseOperator(input.operator()),
                input.priority() == null ? 0 : input.priority()
        )).toList();
    }

    private static PriceRuleOperator parseOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            return PriceRuleOperator.EQ;
        }
        return PriceRuleOperator.valueOf(operator.trim().toUpperCase());
    }

    private static PriceSetResult.PriceResult toPriceSetPrice(Price price) {
        return new PriceSetResult.PriceResult(
                price.getId().getValue(),
                price.getTitle(),
                price.getCurrencyCode().value(),
                price.getAmount().value(),
                price.getMinQuantity(),
                price.getMaxQuantity(),
                price.getPriceSetId().getValue(),
                price.getPriceListId().map(Id::getValue).orElse(null),
                price.getRules().stream().map(PricingResultMapper::toRule).toList()
        );
    }

    private static PriceListResult.PriceResult toPriceListPrice(Price price) {
        return new PriceListResult.PriceResult(
                price.getId().getValue(),
                price.getTitle(),
                price.getCurrencyCode().value(),
                price.getAmount().value(),
                price.getMinQuantity(),
                price.getMaxQuantity(),
                price.getPriceSetId().getValue(),
                price.getPriceListId().map(Id::getValue).orElse(null),
                price.getRules().stream().map(PricingResultMapper::toRule).toList()
        );
    }

    private static PriceSetResult.PriceRuleResult toRule(PriceRule rule) {
        return new PriceSetResult.PriceRuleResult(
                rule.getId().getValue(),
                rule.getAttribute(),
                rule.getValue(),
                rule.getOperator().name(),
                rule.getPriority()
        );
    }

    private static PriceListResult.PriceListRuleResult toListRule(PriceListRule rule) {
        return new PriceListResult.PriceListRuleResult(
                rule.getId().getValue(),
                rule.getAttribute(),
                rule.getValues()
        );
    }

    private static CalculatedPriceSetResult.SelectedPriceResult toSelected(
            CalculatedPriceSet.SelectedPrice selected
    ) {
        return new CalculatedPriceSetResult.SelectedPriceResult(
                selected.priceId() == null ? null : selected.priceId().getValue(),
                selected.priceListId() == null ? null : selected.priceListId().getValue(),
                selected.priceListType() == null ? null : selected.priceListType().name().toLowerCase(),
                selected.minQuantity(),
                selected.maxQuantity()
        );
    }
}

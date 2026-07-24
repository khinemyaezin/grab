package com.pricing.domain.policy;

import com.grab.framework.id.Id;
import com.pricing.domain.enums.PriceListStatus;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.enums.PriceRuleOperator;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import com.pricing.domain.valueobject.PricingContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class CalculatePricesPolicy {

    public List<CalculatedPriceSet> calculate(
            List<Id> priceSetIds,
            PricingContext context,
            List<PriceCandidate> candidates,
            List<PricePreferenceView> preferences,
            Instant now
    ) {
        Objects.requireNonNull(priceSetIds, "priceSetIds is required");
        Objects.requireNonNull(context, "context is required");
        Objects.requireNonNull(candidates, "candidates is required");
        Objects.requireNonNull(now, "now is required");

        Map<String, List<PriceCandidate>> bySet = new HashMap<>();
        for (PriceCandidate candidate : candidates) {
            if (!matchesCurrency(candidate, context)) {
                continue;
            }
            if (!matchesQuantity(candidate, context)) {
                continue;
            }
            if (!matchesPriceListConstraints(candidate, context, now)) {
                continue;
            }
            if (!matchesPriceRules(candidate, context)) {
                continue;
            }
            bySet.computeIfAbsent(candidate.priceSetId().getValue(), key -> new ArrayList<>()).add(candidate);
        }

        boolean taxInclusive = resolveTaxInclusive(preferences, context);
        List<CalculatedPriceSet> results = new ArrayList<>();
        for (Id priceSetId : priceSetIds) {
            List<PriceCandidate> setCandidates = bySet.getOrDefault(priceSetId.getValue(), List.of());
            List<PriceCandidate> ranked = rank(setCandidates);
            results.add(synthesize(priceSetId, context.currencyCode().value(), ranked, taxInclusive));
        }
        return results;
    }

    private boolean matchesCurrency(PriceCandidate candidate, PricingContext context) {
        return context.currencyCode().value().equalsIgnoreCase(candidate.currencyCode());
    }

    private boolean matchesQuantity(PriceCandidate candidate, PricingContext context) {
        int quantity = context.effectiveQuantity();
        boolean minOk = candidate.minQuantity() == null || candidate.minQuantity() <= quantity;
        boolean maxOk = candidate.maxQuantity() == null || candidate.maxQuantity() >= quantity;
        return minOk && maxOk;
    }

    private boolean matchesPriceListConstraints(PriceCandidate candidate, PricingContext context, Instant now) {
        if (!candidate.isPriceListPrice()) {
            return true;
        }
        if (candidate.priceListStatus() != PriceListStatus.ACTIVE) {
            return false;
        }
        boolean started = candidate.startsAt() == null || !candidate.startsAt().isAfter(now);
        boolean notEnded = candidate.endsAt() == null || !candidate.endsAt().isBefore(now);
        if (!started || !notEnded) {
            return false;
        }
        return matchesPriceListRules(candidate, context);
    }

    private boolean matchesPriceListRules(PriceCandidate candidate, PricingContext context) {
        List<PriceCandidate.ListRuleCondition> rules = candidate.priceListRules();
        if (rules == null || rules.isEmpty()) {
            return candidate.priceListRulesCount() == 0;
        }
        if (rules.size() != candidate.priceListRulesCount()) {
            return false;
        }
        for (PriceCandidate.ListRuleCondition rule : rules) {
            String contextValue = context.attributes().get(rule.attribute());
            if (contextValue == null || !rule.values().contains(contextValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesPriceRules(PriceCandidate candidate, PricingContext context) {
        List<PriceCandidate.RuleCondition> rules = candidate.priceRules();
        if (rules == null || rules.isEmpty()) {
            return candidate.rulesCount() == 0;
        }
        if (rules.size() != candidate.rulesCount()) {
            return false;
        }
        int matched = 0;
        for (PriceCandidate.RuleCondition rule : rules) {
            String contextValue = context.attributes().get(rule.attribute());
            if (matchesOperator(rule.operator(), rule.value(), contextValue)) {
                matched++;
            }
        }
        return matched == candidate.rulesCount();
    }

    private boolean matchesOperator(PriceRuleOperator operator, String ruleValue, String contextValue) {
        if (contextValue == null) {
            return false;
        }
        PriceRuleOperator effective = operator == null ? PriceRuleOperator.EQ : operator;
        return switch (effective) {
            case EQ -> ruleValue.equals(contextValue);
            case GT -> compareNumeric(contextValue, ruleValue) > 0;
            case GTE -> compareNumeric(contextValue, ruleValue) >= 0;
            case LT -> compareNumeric(contextValue, ruleValue) < 0;
            case LTE -> compareNumeric(contextValue, ruleValue) <= 0;
        };
    }

    private int compareNumeric(String left, String right) {
        try {
            return Double.compare(Double.parseDouble(left), Double.parseDouble(right));
        } catch (NumberFormatException exception) {
            return left.compareTo(right);
        }
    }

    private List<PriceCandidate> rank(List<PriceCandidate> candidates) {
        return candidates.stream()
                .sorted(Comparator
                        .comparing(PriceCandidate::isPriceListPrice).reversed()
                        .thenComparing(candidate -> candidate.rulesCount() + candidate.priceListRulesCount(),
                                Comparator.reverseOrder())
                        .thenComparing(PriceCandidate::amount))
                .toList();
    }

    private CalculatedPriceSet synthesize(
            Id priceSetId,
            String currencyCode,
            List<PriceCandidate> ranked,
            boolean taxInclusive
    ) {
        PriceCandidate defaultPrice = firstMatching(ranked, candidate -> !candidate.isPriceListPrice());
        PriceCandidate priceListPrice = firstMatching(ranked, PriceCandidate::isPriceListPrice);
        PriceCandidate calculated = resolveCalculated(defaultPrice, priceListPrice);
        PriceCandidate original = resolveOriginal(defaultPrice, ranked);

        return new CalculatedPriceSet(
                priceSetId,
                calculated != null || original != null ? currencyCode : null,
                calculated == null ? null : calculated.amount(),
                calculated != null && calculated.isPriceListPrice(),
                calculated != null && taxInclusive,
                calculated == null ? emptySelected() : toSelected(calculated),
                original == null ? null : original.amount(),
                original != null && original.isPriceListPrice(),
                original != null && taxInclusive,
                original == null ? emptySelected() : toSelected(original)
        );
    }

    private PriceCandidate resolveCalculated(PriceCandidate defaultPrice, PriceCandidate priceListPrice) {
        if (priceListPrice == null) {
            return defaultPrice;
        }
        if (priceListPrice.priceListType() == PriceListType.OVERRIDE) {
            return priceListPrice;
        }
        if (priceListPrice.priceListType() == PriceListType.SALE) {
            if (defaultPrice == null) {
                return priceListPrice;
            }
            return priceListPrice.amount().compareTo(defaultPrice.amount()) <= 0
                    ? priceListPrice
                    : defaultPrice;
        }
        return defaultPrice;
    }

    private PriceCandidate resolveOriginal(PriceCandidate defaultPrice, List<PriceCandidate> ranked) {
        PriceCandidate nonSaleOverride = firstMatching(
                ranked,
                candidate -> candidate.isPriceListPrice()
                        && candidate.priceListType() == PriceListType.OVERRIDE
        );
        if (nonSaleOverride != null) {
            return nonSaleOverride;
        }
        return defaultPrice;
    }

    private boolean resolveTaxInclusive(List<PricePreferenceView> preferences, PricingContext context) {
        if (preferences == null || preferences.isEmpty()) {
            return false;
        }
        String regionId = context.attributes().get(PricingAttributeKeys.REGION_ID);
        if (regionId != null) {
            for (PricePreferenceView preference : preferences) {
                if (PricingAttributeKeys.REGION_ID.equals(preference.attribute())
                        && regionId.equals(preference.value())) {
                    return preference.taxInclusive();
                }
            }
        }
        String currencyCode = context.currencyCode().value();
        for (PricePreferenceView preference : preferences) {
            if (PricingAttributeKeys.CURRENCY_CODE.equals(preference.attribute())
                    && currencyCode.equalsIgnoreCase(preference.value())) {
                return preference.taxInclusive();
            }
        }
        return false;
    }

    private PriceCandidate firstMatching(
            List<PriceCandidate> candidates,
            Predicate<PriceCandidate> predicate
    ) {
        for (PriceCandidate candidate : candidates) {
            if (predicate.test(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private CalculatedPriceSet.SelectedPrice toSelected(PriceCandidate candidate) {
        return new CalculatedPriceSet.SelectedPrice(
                candidate.priceId(),
                candidate.priceListId(),
                candidate.priceListType(),
                candidate.minQuantity(),
                candidate.maxQuantity()
        );
    }

    private CalculatedPriceSet.SelectedPrice emptySelected() {
        return new CalculatedPriceSet.SelectedPrice(null, null, null, null, null);
    }
}

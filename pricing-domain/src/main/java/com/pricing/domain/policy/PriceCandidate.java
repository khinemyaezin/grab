package com.pricing.domain.policy;

import com.grab.framework.id.Id;
import com.pricing.domain.enums.PriceListStatus;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.enums.PriceRuleOperator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PriceCandidate(
        Id priceId,
        Id priceSetId,
        Id priceListId,
        PriceListType priceListType,
        PriceListStatus priceListStatus,
        Instant startsAt,
        Instant endsAt,
        String currencyCode,
        BigDecimal amount,
        Integer minQuantity,
        Integer maxQuantity,
        int rulesCount,
        int priceListRulesCount,
        List<RuleCondition> priceRules,
        List<ListRuleCondition> priceListRules
) {
    public record RuleCondition(String attribute, String value, PriceRuleOperator operator) {
    }

    public record ListRuleCondition(String attribute, List<String> values) {
    }

    public boolean isPriceListPrice() {
        return priceListId != null;
    }
}

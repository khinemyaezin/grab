package com.pricing.domain.entity;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public class Price extends Entity<Id> {
    private String title;
    private CurrencyCode currencyCode;
    private MoneyAmount amount;
    private Integer minQuantity;
    private Integer maxQuantity;
    private final Id priceSetId;
    private final Id priceListId;
    private final List<PriceRule> rules;

    public Price(
            Id id,
            String title,
            CurrencyCode currencyCode,
            MoneyAmount amount,
            Integer minQuantity,
            Integer maxQuantity,
            Id priceSetId,
            Id priceListId,
            List<PriceRule> rules
    ) {
        super(id);
        this.priceSetId = Objects.requireNonNull(priceSetId, "priceSetId is required");
        this.priceListId = priceListId;
        this.rules = new ArrayList<>(rules == null ? List.of() : rules);
        replaceDetails(title, currencyCode, amount, minQuantity, maxQuantity);
    }

    public static Price createBase(
            Id id,
            Id priceSetId,
            String title,
            CurrencyCode currencyCode,
            MoneyAmount amount,
            Integer minQuantity,
            Integer maxQuantity,
            List<PriceRule> rules
    ) {
        return new Price(id, title, currencyCode, amount, minQuantity, maxQuantity, priceSetId, null, rules);
    }

    public static Price createCampaign(
            Id id,
            Id priceSetId,
            Id priceListId,
            String title,
            CurrencyCode currencyCode,
            MoneyAmount amount,
            Integer minQuantity,
            Integer maxQuantity,
            List<PriceRule> rules
    ) {
        Objects.requireNonNull(priceListId, "priceListId is required");
        return new Price(id, title, currencyCode, amount, minQuantity, maxQuantity, priceSetId, priceListId, rules);
    }

    public void replaceDetails(
            String title,
            CurrencyCode currencyCode,
            MoneyAmount amount,
            Integer minQuantity,
            Integer maxQuantity
    ) {
        validateQuantityRange(minQuantity, maxQuantity);
        this.title = title;
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode is required");
        this.amount = Objects.requireNonNull(amount, "amount is required");
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    public void replaceRules(List<PriceRule> nextRules) {
        rules.clear();
        if (nextRules != null) {
            rules.addAll(nextRules);
        }
    }

    public int rulesCount() {
        return rules.size();
    }

    public List<PriceRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public Optional<Id> getPriceListId() {
        return Optional.ofNullable(priceListId);
    }

    public boolean isBasePrice() {
        return priceListId == null;
    }

    public boolean matchesQuantity(int quantity) {
        boolean minOk = minQuantity == null || minQuantity <= quantity;
        boolean maxOk = maxQuantity == null || maxQuantity >= quantity;
        return minOk && maxOk;
    }

    public boolean matchesRules(java.util.Map<String, String> attributes) {
        if (rules.isEmpty()) {
            return true;
        }
        int matched = 0;
        for (PriceRule rule : rules) {
            String contextValue = attributes.get(rule.getAttribute());
            if (rule.matches(contextValue)) {
                matched++;
            }
        }
        return matched == rules.size();
    }

    private static void validateQuantityRange(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && maxQuantity != null && minQuantity > maxQuantity) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidQuantityRange(minQuantity, maxQuantity),
                    "minQuantity must be less than or equal to maxQuantity"
            );
        }
    }
}

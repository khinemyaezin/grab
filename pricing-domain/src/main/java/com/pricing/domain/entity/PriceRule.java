package com.pricing.domain.entity;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.pricing.domain.enums.PriceRuleOperator;
import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class PriceRule extends Entity<Id> {
    private final String attribute;
    private final String value;
    private final PriceRuleOperator operator;
    private final int priority;

    public PriceRule(Id id, String attribute, String value, PriceRuleOperator operator, int priority) {
        super(id);
        if (attribute == null || attribute.isBlank()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("attribute"),
                    "price rule attribute is required"
            );
        }
        if (value == null || value.isBlank()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("value"),
                    "price rule value is required"
            );
        }
        this.attribute = attribute.trim();
        this.value = value.trim();
        this.operator = operator == null ? PriceRuleOperator.EQ : operator;
        this.priority = priority;
    }

    public boolean matches(String contextValue) {
        if (contextValue == null) {
            return false;
        }
        return switch (operator) {
            case EQ -> value.equals(contextValue);
            case GT -> compareNumeric(contextValue) > 0;
            case GTE -> compareNumeric(contextValue) >= 0;
            case LT -> compareNumeric(contextValue) < 0;
            case LTE -> compareNumeric(contextValue) <= 0;
        };
    }

    private int compareNumeric(String contextValue) {
        try {
            double left = Double.parseDouble(contextValue);
            double right = Double.parseDouble(value);
            return Double.compare(left, right);
        } catch (NumberFormatException exception) {
            return contextValue.compareTo(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

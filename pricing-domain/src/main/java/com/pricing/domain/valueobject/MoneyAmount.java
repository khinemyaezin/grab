package com.pricing.domain.valueobject;

import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;

import java.math.BigDecimal;
import java.util.Objects;

public record MoneyAmount(BigDecimal value) {
    public MoneyAmount {
        if (value == null) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("amount"),
                    "amount is required"
            );
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("amount"),
                    "amount must be non-negative"
            );
        }
    }

    public static MoneyAmount of(BigDecimal value) {
        return new MoneyAmount(value);
    }

    public boolean isLessThan(MoneyAmount other) {
        return value.compareTo(other.value) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoneyAmount that)) {
            return false;
        }
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }
}

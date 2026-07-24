package com.pricing.domain.valueobject;

import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;

import java.util.Locale;
import java.util.Objects;

public record CurrencyCode(String value) {
    public CurrencyCode {
        if (value == null || value.isBlank()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("currencyCode"),
                    "currencyCode is required"
            );
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    public static CurrencyCode of(String value) {
        return new CurrencyCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CurrencyCode that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

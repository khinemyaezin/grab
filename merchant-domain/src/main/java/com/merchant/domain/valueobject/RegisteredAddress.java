package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

import java.util.Locale;

public record RegisteredAddress(
        String line1,
        String line2,
        String city,
        String region,
        String postalCode,
        String countryCode
) {
    public RegisteredAddress {
        line1 = required(line1, "addressLine1");
        line2 = optional(line2);
        city = required(city, "addressCity");
        region = optional(region);
        postalCode = required(postalCode, "addressPostalCode");
        if (countryCode == null || !countryCode.trim().matches("[A-Za-z]{2}")) {
            throw invalid("addressCountryCode");
        }
        countryCode = countryCode.trim().toUpperCase(Locale.ROOT);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw invalid(field);
        return value.trim();
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static MerchantDomainException invalid(String field) {
        return new MerchantDomainException(new MerchantDomainError.InvalidField(field), field + " is invalid");
    }
}

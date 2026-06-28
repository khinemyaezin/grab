package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

import java.util.Locale;

public record BusinessRegistration(
        String countryCode,
        String registrationNumber) {
    public BusinessRegistration {
        if (countryCode == null || !countryCode.trim().matches("[A-Za-z]{2}")) {
            throw invalid("registrationCountryCode");
        }
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw invalid("registrationNumber");
        }
        countryCode = countryCode.trim().toUpperCase(Locale.ROOT);
        registrationNumber = registrationNumber.trim().toUpperCase(Locale.ROOT);
    }

    private static MerchantDomainException invalid(String field) {
        return new MerchantDomainException(new MerchantDomainError.InvalidField(field), field + " is invalid");
    }
}

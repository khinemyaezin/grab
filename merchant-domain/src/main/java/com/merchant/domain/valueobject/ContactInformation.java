package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

import java.util.Locale;

public record ContactInformation(String email, String phone) {
    public ContactInformation {
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw invalid("contactEmail");
        }
        if (phone == null || phone.isBlank()) {
            throw invalid("contactPhone");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        phone = phone.trim();
    }

    private static MerchantDomainException invalid(String field) {
        return new MerchantDomainException(new MerchantDomainError.InvalidField(field), field + " is invalid");
    }
}

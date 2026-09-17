package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

import java.util.Locale;
import java.util.regex.Pattern;

public record StorefrontSlug(String value) {
    private static final int MAX_LENGTH = 255;
    private static final Pattern KEBAB_CASE = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    public StorefrontSlug {
        if (value == null || value.isBlank()) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("slug"),
                    "Storefront slug is required"
            );
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH || !KEBAB_CASE.matcher(value).matches()) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("slug"),
                    "Storefront slug must be lowercase kebab-case"
            );
        }
    }
}

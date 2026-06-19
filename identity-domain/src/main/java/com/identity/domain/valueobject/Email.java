package com.identity.domain.valueobject;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        String suppliedValue = value;
        if (value == null || !FORMAT.matcher(value.trim()).matches()) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidEmail(String.valueOf(suppliedValue)),
                    "A valid email address is required"
            );
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }
}

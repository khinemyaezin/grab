package com.identity.domain.valueobject;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScopeKeyTest {
    @Test
    void create_withNamespacedValue_shouldNormalizeCaseAndWhitespace() {
        ScopeKey key = new ScopeKey("  Merchant.Storefront  ");

        assertEquals("merchant.storefront", key.value());
    }

    @Test
    void create_withGlobalValue_shouldCreateBuiltInKey() {
        assertEquals("global", ScopeKey.global().value());
    }

    @Test
    void create_withUnnamespacedResource_shouldRejectKey() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> new ScopeKey("MERCHANT_ACCOUNT")
        );

        assertInstanceOf(IdentityDomainError.InvalidScopeKey.class, exception.getMessageSource());
    }

    @Test
    void create_withMalformedNamespace_shouldRejectKey() {
        assertThrows(IdentityDomainValidationException.class, () -> new ScopeKey("merchant..account"));
        assertThrows(IdentityDomainValidationException.class, () -> new ScopeKey("merchant_account"));
    }
}

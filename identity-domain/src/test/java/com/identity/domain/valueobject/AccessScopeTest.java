package com.identity.domain.valueobject;

import com.identity.domain.enums.AccessScopeType;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessScopeTest {
    @Test
    void globalScope_encompassesAnyTargetScope() {
        AccessScope target = new AccessScope(AccessScopeType.MERCHANT_ACCOUNT, "merchant-1");

        assertTrue(AccessScope.global().encompasses(target));
        assertDoesNotThrow(() -> AccessScope.global().requireEncompasses(target));
    }

    @Test
    void exactScope_encompassesOnlyTheSameScope() {
        AccessScope actor = new AccessScope(AccessScopeType.MERCHANT_ACCOUNT, "merchant-1");

        assertTrue(actor.encompasses(new AccessScope(AccessScopeType.MERCHANT_ACCOUNT, "merchant-1")));
        assertFalse(actor.encompasses(new AccessScope(AccessScopeType.MERCHANT_ACCOUNT, "merchant-2")));
    }

    @Test
    void requireEncompasses_withDifferentScope_shouldRejectAccess() {
        AccessScope actor = new AccessScope(AccessScopeType.MERCHANT_ACCOUNT, "merchant-1");
        AccessScope target = new AccessScope(AccessScopeType.MERCHANT_ACCOUNT, "merchant-2");

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> actor.requireEncompasses(target)
        );

        assertInstanceOf(IdentityDomainError.AccessScopeNotEncompassed.class,
                exception.getMessageSource());
    }
}

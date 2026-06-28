package com.identity.domain.service;

import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessRoleDelegationPolicyTest {
    private final AccessRoleDelegationPolicy policy = new AccessRoleDelegationPolicy();

    @Test
    void merchantOwner_canDelegateMerchantAdminButNotOwner() {
        assertDoesNotThrow(() -> policy.requireCanDelegate(
                Set.of("MERCHANT_OWNER"), "MERCHANT_ADMIN"
        ));

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.requireCanDelegate(Set.of("MERCHANT_OWNER"), "MERCHANT_OWNER")
        );

        assertInstanceOf(IdentityDomainError.AccessRoleDelegationForbidden.class,
                exception.getMessageSource());
    }

    @Test
    void merchantAdmin_canDelegateStaffButNotAnotherAdmin() {
        assertDoesNotThrow(() -> policy.requireCanDelegate(
                Set.of("MERCHANT_ADMIN"), "CATALOG_MANAGER"
        ));
        assertThrows(
                IdentityDomainValidationException.class,
                () -> policy.requireCanDelegate(Set.of("MERCHANT_ADMIN"), "MERCHANT_ADMIN")
        );
    }

    @Test
    void platformAdmin_canDelegateAnyConfiguredRole() {
        assertDoesNotThrow(() -> policy.requireCanDelegate(Set.of("USER_ADMIN"), "MERCHANT_OWNER"));
        assertDoesNotThrow(() -> policy.requireCanDelegate(Set.of("SUPER_ADMIN"), "CUSTOMER"));
    }
}

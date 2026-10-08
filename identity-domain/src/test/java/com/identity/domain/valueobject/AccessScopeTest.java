package com.identity.domain.valueobject;

import com.identity.domain.ScopeHierarchyTestExtension;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ScopeHierarchyTestExtension.class)
class AccessScopeTest {
    @Test
    void globalScope_encompassesAnyTargetScope() {
        AccessScope target = AccessScope.from("merchant.account", "merchant-1");

        assertTrue(AccessScope.global().encompasses(target));
        assertDoesNotThrow(() -> AccessScope.global().requireEncompasses(target));
    }

    @Test
    void exactScope_encompassesOnlyTheSameScope() {
        AccessScope actor = AccessScope.from("merchant.account", "merchant-1");

        assertTrue(actor.encompasses(AccessScope.from("merchant.account", "merchant-1")));
        assertFalse(actor.encompasses(AccessScope.from("merchant.account", "merchant-2")));
    }

    @Test
    void merchantAccountScope_encompassesChildScopeTypes() {
        AccessScope merchant = AccessScope.from("merchant.account", "merchant-1");
        AccessScope storefront = AccessScope.from("merchant.storefront", "sf-1");
        AccessScope fulfillmentLocation =
                AccessScope.from("inventory.fulfillment-location", "loc-1");

        assertTrue(merchant.encompasses(storefront));
        assertTrue(merchant.encompasses(fulfillmentLocation));
    }

    @Test
    void storefrontScope_doesNotEncompassSiblingOrParent() {
        AccessScope storefront = AccessScope.from("merchant.storefront", "sf-1");
        AccessScope fulfillmentLocation =
                AccessScope.from("inventory.fulfillment-location", "loc-1");
        AccessScope merchant = AccessScope.from("merchant.account", "merchant-1");

        assertFalse(storefront.encompasses(fulfillmentLocation));
        assertFalse(storefront.encompasses(merchant));
    }

    @Test
    void fulfillmentScope_doesNotEncompassSiblingOrParent() {
        AccessScope location = AccessScope.from("inventory.fulfillment-location", "loc-1");
        AccessScope storefront = AccessScope.from("merchant.storefront", "sf-1");
        AccessScope merchant = AccessScope.from("merchant.account", "merchant-1");

        assertFalse(location.encompasses(storefront));
        assertFalse(location.encompasses(merchant));
    }

    @Test
    void requireEncompasses_withDifferentScope_shouldRejectAccess() {
        AccessScope actor = AccessScope.from("merchant.account", "merchant-1");
        AccessScope target = AccessScope.from("merchant.account", "merchant-2");

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> actor.requireEncompasses(target)
        );

        assertInstanceOf(IdentityDomainError.AccessScopeNotEncompassed.class,
                exception.getMessageSource());
    }
}

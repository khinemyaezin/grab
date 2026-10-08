package com.identity.domain.valueobject;

import com.grab.framework.security.ScopeDeclaration;
import com.identity.domain.ScopeHierarchyTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ScopeHierarchyTestExtension.class)
class ScopeHierarchyTest {

    @ParameterizedTest
    @CsvSource({
            "global, merchant.account, true",
            "global, merchant.storefront, true",
            "global, inventory.fulfillment-location, true",
            "merchant.account, merchant.storefront, true",
            "merchant.account, inventory.fulfillment-location, true",
            "merchant.account, merchant.account, true",
            "merchant.storefront, merchant.storefront, true",
            "merchant.storefront, merchant.account, false",
            "merchant.storefront, inventory.fulfillment-location, false",
            "inventory.fulfillment-location, merchant.storefront, false",
            "inventory.fulfillment-location, merchant.account, false"
    })
    void isAncestorOrSelf(String actorKey, String targetKey, boolean expected) {
        String normalizedActorKey = actorKey.trim();
        String normalizedTargetKey = targetKey.trim();
        boolean actual = ScopeHierarchy.isAncestorOrSelf(normalizedActorKey, normalizedTargetKey);
        assertEquals(expected, actual);
    }

    @Test
    void parentOf_storefrontScope_shouldReturnMerchantAccount() {
        var parent = ScopeHierarchy.parentOf("merchant.storefront");
        assertEquals("merchant.account", parent.orElse(null));
    }

    @Test
    void parentOf_fulfillmentLocationScope_shouldReturnMerchantAccount() {
        var parent = ScopeHierarchy.parentOf("inventory.fulfillment-location");
        assertEquals("merchant.account", parent.orElse(null));
    }

    @Test
    void parentOf_merchantAccountScope_shouldReturnEmpty() {
        assertTrue(ScopeHierarchy.parentOf("merchant.account").isEmpty());
    }

    @Test
    void isRegistered_shouldRecognizeAllKnownKeys() {
        assertTrue(ScopeHierarchy.isRegistered("global"));
        assertTrue(ScopeHierarchy.isRegistered("merchant.account"));
        assertTrue(ScopeHierarchy.isRegistered("merchant.storefront"));
        assertTrue(ScopeHierarchy.isRegistered("inventory.fulfillment-location"));
        assertFalse(ScopeHierarchy.isRegistered("some.unknown.scope"));
    }

    @Test
    void register_withSelfAsParent_shouldThrowIllegalArgumentException() {
        var declaration = new ScopeDeclaration("test.self-scope", "test.self-scope");
        assertThrows(IllegalArgumentException.class, () -> ScopeHierarchy.register(declaration));
    }

    @Test
    void register_withCycle_shouldThrowIllegalArgumentException() {
        ScopeHierarchy.register(new ScopeDeclaration("test.cycle-a", "test.cycle-b"));
        var cycleDeclaration = new ScopeDeclaration("test.cycle-b", "test.cycle-a");
        assertThrows(IllegalArgumentException.class, () -> ScopeHierarchy.register(cycleDeclaration));
    }

    @Test
    void allKeys_shouldContainGlobalAndRegisteredKeys() {
        var keys = ScopeHierarchy.allKeys();
        assertTrue(keys.contains("global"));
        assertTrue(keys.contains("merchant.account"));
        assertTrue(keys.contains("merchant.storefront"));
        assertTrue(keys.contains("inventory.fulfillment-location"));
    }
}

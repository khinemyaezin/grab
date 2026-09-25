package com.identity.domain.model;

import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorityTest {

    @Test
    void fromManifest_usesUuidIdentityAndModuleCategory() {
        CommonId id = new CommonId("authority-uuid");

        Authority authority = Authority.from(
                id,
                "merchant",
                new AuthorityDefinition("MERCHANT_PROFILE_READ", "Read merchant profile", "View merchant profile")
        );

        assertEquals(id, authority.getId());
        assertEquals("MERCHANT_PROFILE_READ", authority.getCode());
        assertEquals("merchant", authority.getCategory());
        assertTrue(authority.isActive());
    }

    @Test
    void rehydrate_preservesInactiveState() {
        Authority authority = Authority.rehydrate(
                new CommonId("authority-uuid"),
                "MERCHANT_PROFILE_READ",
                "merchant",
                "Read merchant profile",
                null,
                false
        );

        assertFalse(authority.isActive());
    }

    @Test
    void authorities_withSameUuid_areEqualRegardlessOfMetadata() {
        CommonId id = new CommonId("authority-uuid");
        Authority first = Authority.rehydrate(id, "MERCHANT_PROFILE_READ", "merchant", "Read", null, true);
        Authority second = Authority.rehydrate(id, "MERCHANT_PROFILE_WRITE", "merchant", "Write", null, false);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, Authority.rehydrate(
                new CommonId("other-uuid"), "MERCHANT_PROFILE_READ", "merchant", "Read", null, true
        ));
    }

    @Test
    void category_isRequired() {
        assertThrows(NullPointerException.class, () -> Authority.rehydrate(
                new CommonId("authority-uuid"),
                "MERCHANT_PROFILE_READ",
                null,
                "Read merchant profile",
                null,
                true
        ));
    }
}

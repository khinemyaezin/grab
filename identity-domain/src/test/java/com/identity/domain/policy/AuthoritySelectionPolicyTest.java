package com.identity.domain.policy;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.exception.IdentityDomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthoritySelectionPolicyTest {
    @Test
    void normalize_invalidCode_rejectsEntireRequest() {
        assertThrows(IdentityDomainValidationException.class, () -> AuthoritySelectionPolicy.normalize(Set.of("USER_READ", "*")));
        assertEquals(Set.of("USER_READ"), AuthoritySelectionPolicy.normalize(Set.of(" user_read ")));
    }

    @Test
    void requireComplete_unknownOrDisabledAuthority_rejectsPartialGrant() {
        var enabled = Authority.rehydrate(new CommonId("read"), "USER_READ", "identity", "Read", null, true);
        var disabled = Authority.rehydrate(new CommonId("write"), "USER_WRITE", "identity", "Write", null, false);
        assertThrows(IdentityDomainValidationException.class, () -> AuthoritySelectionPolicy.requireComplete(Set.of("USER_READ", "USER_WRITE"), Set.of(enabled)));
        assertThrows(IdentityDomainValidationException.class, () -> AuthoritySelectionPolicy.requireComplete(Set.of("USER_WRITE"), Set.of(disabled)));
        assertDoesNotThrow(() -> AuthoritySelectionPolicy.requireComplete(Set.of("USER_READ"), Set.of(enabled)));
    }
}

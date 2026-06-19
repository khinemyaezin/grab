package com.grab.framework.security;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticatedActorTest {
    @Test void constructor_withMutableSets_shouldCreateDefensiveCopies() {
        Set<String> roles = new HashSet<>(Set.of("SELLER"));
        AuthenticatedActor actor = new AuthenticatedActor("u1", "issuer", "subject", "a@example.com", roles, Set.of("PRODUCT_WRITE_OWN"));
        roles.add("ADMIN");
        assertEquals(Set.of("SELLER"), actor.roles());
        assertThrows(UnsupportedOperationException.class, () -> actor.roles().add("ADMIN"));
    }
}

package com.grab.store.shared.security;

import com.grab.framework.security.AuthenticatedActor;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPrincipalTest {

    @Test
    void contextAccessors_withContextFreeActor_shouldReturnEmpty() {
        SecurityPrincipal principal = new SecurityPrincipal(new AuthenticatedActor(
                "user-1",
                "local-issuer",
                "user-1",
                "customer@example.com",
                Set.of(),
                Set.of()
        ));

        assertThat(principal.getAccessContext()).isEmpty();
        assertThat(principal.getPlatformCode()).isEmpty();
        assertThat(principal.getScopeKey()).isEmpty();
        assertThat(principal.getScopeId()).isEmpty();
    }
}

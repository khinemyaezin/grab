package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityLookupQueryAdapterTest {

    @Mock
    private IdentityLookupUseCase identityLookupUseCase;

    private IdentityLookupQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new IdentityLookupQueryAdapter(identityLookupUseCase);
    }

    @Test
    void resolveByPlatformUserId_existingActor_returnsAuthenticatedActor() {
        AccessContext context = new AccessContext("MERCHANT", "assign-1", "STORE", "store-1");
        AuthenticatedActor actor = new AuthenticatedActor(
                "user-1", "MERCHANT", "sub-1", "user@test.com",
                Set.of("MERCHANT_ADMIN"), Set.of("PERM_READ"), context
        );
        when(identityLookupUseCase.resolveByPlatformUserId("MERCHANT", "user-1", context))
                .thenReturn(Optional.of(actor));

        Optional<AuthenticatedActor> result = adapter.resolveByPlatformUserId("MERCHANT", "user-1", context);

        assertThat(result).isPresent();
        assertThat(result.get().platformUserId()).isEqualTo("user-1");
    }

    @Test
    void resolveByExternalIdentity_existingActor_returnsAuthenticatedActor() {
        AccessContext context = new AccessContext("MERCHANT", "assign-1", "STORE", "store-1");
        AuthenticatedActor actor = new AuthenticatedActor(
                "user-1", "MERCHANT", "sub-1", "user@test.com",
                Set.of("MERCHANT_ADMIN"), Set.of("PERM_READ"), context
        );
        when(identityLookupUseCase.resolveByExternalIdentity("issuer-1", "sub-1", Set.of("scope1"), context))
                .thenReturn(Optional.of(actor));

        Optional<AuthenticatedActor> result = adapter.resolveByExternalIdentity("issuer-1", "sub-1", Set.of("scope1"), context);

        assertThat(result).isPresent();
        assertThat(result.get().platformUserId()).isEqualTo("user-1");
    }
}

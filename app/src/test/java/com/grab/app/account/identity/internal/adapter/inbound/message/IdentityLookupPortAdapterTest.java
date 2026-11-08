package com.grab.app.account.identity.internal.adapter.inbound.message;

import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ResolveExternalIdentityPortMapperImpl;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ResolvePlatformUserPortMapperImpl;
import com.grab.app.account.identity.internal.query.handler.ResolveExternalIdentityQueryHandler;
import com.grab.app.account.identity.internal.query.handler.ResolvePlatformUserQueryHandler;
import com.grab.app.account.identity.port.message.IdentityLookupPort;
import com.grab.app.account.identity.port.message.IdentityLookupPort.AccessContextMessage;
import com.grab.app.account.identity.port.message.IdentityLookupPort.AuthenticatedActorResult;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.cqrs.query.impl.DefaultQueryBus;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.identity.application.port.inbound.IdentityLookupUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityLookupPortAdapterTest {

    private IdentityLookupUseCase fakeUseCase;
    private IdentityLookupPort portAdapter;

    @BeforeEach
    void setUp() {
        fakeUseCase = new IdentityLookupUseCase() {
            @Override
            public Optional<AuthenticatedActor> resolveByPlatformUserId(String issuer, String userId, AccessContext accessContext) {
                if ("user-123".equals(userId)) {
                    return Optional.of(new AuthenticatedActor(
                            userId,
                            issuer,
                            "sub-123",
                            "user@grab.com",
                            Set.of("ROLE_SELLER"),
                            Set.of("merchant:read"),
                            accessContext,
                            1L
                    ));
                }
                return Optional.empty();
            }

            @Override
            public Optional<AuthenticatedActor> resolveByExternalIdentity(String issuer, String subject, Set<String> entitlements, AccessContext accessContext) {
                if ("ext-sub".equals(subject)) {
                    return Optional.of(new AuthenticatedActor(
                            "user-456",
                            issuer,
                            subject,
                            "ext@grab.com",
                            Set.of("ROLE_BUYER"),
                            Set.of("order:create"),
                            accessContext,
                            2L
                    ));
                }
                return Optional.empty();
            }
        };

        var platformUserHandler = new ResolvePlatformUserQueryHandler(fakeUseCase);
        var externalIdentityHandler = new ResolveExternalIdentityQueryHandler(fakeUseCase);

        QueryBus queryBus = new DefaultQueryBus(List.of(platformUserHandler, externalIdentityHandler));

        portAdapter = new IdentityLookupPortAdapter(
                queryBus,
                new ResolvePlatformUserPortMapperImpl(),
                new ResolveExternalIdentityPortMapperImpl()
        );
    }

    @Test
    void shouldResolveByPlatformUserIdThroughQueryBus() {
        var context = new AccessContextMessage("asgn-1", "merchant", "m-100");

        Optional<AuthenticatedActorResult> result = portAdapter.resolveByPlatformUserId("https://identity.grab", "user-123", context);

        assertThat(result).isPresent();
        AuthenticatedActorResult actor = result.get();
        assertThat(actor.platformUserId()).isEqualTo("user-123");
        assertThat(actor.issuer()).isEqualTo("https://identity.grab");
        assertThat(actor.email()).isEqualTo("user@grab.com");
        assertThat(actor.roles()).containsExactly("ROLE_SELLER");
        assertThat(actor.authorities()).containsExactly("merchant:read");
        assertThat(actor.accessContext()).isNotNull();
        assertThat(actor.accessContext().scopeKey()).isEqualTo("merchant");
        assertThat(actor.accessContext().scopeId()).isEqualTo("m-100");
    }

    @Test
    void shouldReturnEmptyWhenPlatformUserNotFound() {
        Optional<AuthenticatedActorResult> result = portAdapter.resolveByPlatformUserId("https://identity.grab", "unknown", null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldResolveByExternalIdentityThroughQueryBus() {
        var context = new AccessContextMessage("asgn-2", "store", "s-200");

        Optional<AuthenticatedActorResult> result = portAdapter.resolveByExternalIdentity(
                "https://auth0.grab",
                "ext-sub",
                Set.of("entitlement-1"),
                context
        );

        assertThat(result).isPresent();
        AuthenticatedActorResult actor = result.get();
        assertThat(actor.platformUserId()).isEqualTo("user-456");
        assertThat(actor.subject()).isEqualTo("ext-sub");
        assertThat(actor.email()).isEqualTo("ext@grab.com");
        assertThat(actor.roles()).containsExactly("ROLE_BUYER");
        assertThat(actor.authorities()).containsExactly("order:create");
        assertThat(actor.authenticationVersion()).isEqualTo(2L);
    }

    @Test
    void shouldReturnEmptyWhenExternalIdentityNotFound() {
        Optional<AuthenticatedActorResult> result = portAdapter.resolveByExternalIdentity(
                "https://auth0.grab",
                "unknown-sub",
                Set.of(),
                null
        );

        assertThat(result).isEmpty();
    }
}


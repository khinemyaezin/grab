package com.grab.store.shared.security;

import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.service.IdentityLookupPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultIdentityResolverClientTest {

    @Mock
    private IdentityLookupPort identityLookup;
    @Mock
    private LocalJwtProperties properties;

    @InjectMocks
    private IdentityResolver resolver;

    @BeforeEach
    void setUp() {
    }

    @Test
    void resolve_withActiveUserWithoutContext_shouldReturnIdentityOnlyActor() {
        when(properties.issuer()).thenReturn("test-issuer");

        String userId = UUID.randomUUID().toString();
        ExternalPrincipal principal = new ExternalPrincipal("test-issuer", userId, "test@example.com", Set.of());

        AuthenticatedActor mockActor = new AuthenticatedActor(userId, "test-issuer", userId, "test@example.com", Set.of(), Set.of());
        when(identityLookup.resolveByPlatformUserId("test-issuer", userId, null)).thenReturn(Optional.of(mockActor));

        AuthenticatedActor actor = resolver.resolve(principal);

        assertEquals(userId, actor.platformUserId());
        assertEquals("test-issuer", actor.issuer());
        assertEquals(userId, actor.subject());
        assertTrue(actor.roles().isEmpty());
        assertTrue(actor.authorities().isEmpty());
    }

    @Test
    void resolve_withSuspendedUser_shouldRejectAuthentication() {
        when(properties.issuer()).thenReturn("test-issuer");

        String userId = UUID.randomUUID().toString();
        ExternalPrincipal principal = new ExternalPrincipal("test-issuer", userId, "test@example.com", Set.of());

        when(identityLookup.resolveByPlatformUserId("test-issuer", userId, null))
                .thenThrow(new IdentityDomainValidationException(new IdentityDomainError.AccountNotActive(userId), "Account is not active"));

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> resolver.resolve(principal));
        assertInstanceOf(IdentitySecurityError.AccountNotActive.class, exception.getMessageSource());
    }

    @Test
    void resolve_withMissingIdentity_shouldRejectAuthentication() {
        when(properties.issuer()).thenReturn("test-issuer");

        String userId = UUID.randomUUID().toString();
        ExternalPrincipal principal = new ExternalPrincipal("test-issuer", userId, "test@example.com", Set.of());

        when(identityLookup.resolveByPlatformUserId("test-issuer", userId, null)).thenReturn(Optional.empty());

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> resolver.resolve(principal));
        assertInstanceOf(IdentitySecurityError.IdentityNotLinked.class, exception.getMessageSource());
    }

    @Test
    void resolve_withExternalIdentity_shouldReturnAuthenticatedActor() {
        when(properties.issuer()).thenReturn("local-issuer");

        String externalIssuer = "external-issuer";
        String externalSubject = "ext-sub";
        ExternalPrincipal principal = new ExternalPrincipal(externalIssuer, externalSubject, "test@example.com", Set.of("ext-role"));

        AuthenticatedActor mockActor = new AuthenticatedActor(UUID.randomUUID().toString(), externalIssuer, externalSubject, "test@example.com", Set.of("SELLER"), Set.of());
        
        when(identityLookup.resolveByExternalIdentity(externalIssuer, externalSubject, Set.of("ext-role"), null))
                .thenReturn(Optional.of(mockActor));

        AuthenticatedActor actor = resolver.resolve(principal);

        assertEquals(externalIssuer, actor.issuer());
        assertEquals(externalSubject, actor.subject());
        assertEquals(Set.of("SELLER"), actor.roles());
    }

    @Test
    void resolve_withoutContextAndWithScopedAssignments_shouldReturnSelectionOnlyActor() {
        when(properties.issuer()).thenReturn("test-issuer");
        String userId = UUID.randomUUID().toString();
        ExternalPrincipal principal = new ExternalPrincipal(
                "test-issuer", userId, "owner@example.com", Set.of("legacy-seller")
        );
        
        AuthenticatedActor mockActor = new AuthenticatedActor(userId, "test-issuer", userId, "owner@example.com", Set.of(), Set.of());
        when(identityLookup.resolveByPlatformUserId("test-issuer", userId, null)).thenReturn(Optional.of(mockActor));

        AuthenticatedActor actor = resolver.resolve(principal);

        assertTrue(actor.roles().isEmpty());
        assertTrue(actor.authorities().isEmpty());
        assertNull(actor.accessContext());
    }

    @Test
    void resolve_withActiveScopedAssignment_shouldReturnOnlyScopedRole() {
        when(properties.issuer()).thenReturn("test-issuer");
        String userId = UUID.randomUUID().toString();
        AccessContext context = new AccessContext(
                "SELLER_PORTAL", "assignment-1", "merchant.account", "merchant-1"
        );
        ExternalPrincipal principal = new ExternalPrincipal(
                "test-issuer", userId, "owner@example.com", Set.of(), context
        );
        
        AuthenticatedActor mockActor = new AuthenticatedActor(userId, "test-issuer", userId, "owner@example.com", Set.of("MERCHANT_OWNER"), Set.of("MERCHANT_WRITE_OWN"), context);;
        when(identityLookup.resolveByPlatformUserId("test-issuer", userId, context)).thenReturn(Optional.of(mockActor));

        AuthenticatedActor actor = resolver.resolve(principal);

        assertEquals(Set.of("MERCHANT_OWNER"), actor.roles());
        assertEquals(Set.of("MERCHANT_WRITE_OWN"), actor.authorities());
        assertEquals(context, actor.accessContext());
    }

    @Test
    void resolve_withMismatchedScopedAssignment_shouldRejectAuthentication() {
        when(properties.issuer()).thenReturn("test-issuer");
        String userId = UUID.randomUUID().toString();
        AccessContext context = new AccessContext(
                "SELLER_PORTAL", "assignment-1", "merchant.account", "merchant-other"
        );
        ExternalPrincipal principal = new ExternalPrincipal(
                "test-issuer", userId, "", Set.of(), context
        );
        
        when(identityLookup.resolveByPlatformUserId("test-issuer", userId, context))
                .thenThrow(new IdentityDomainValidationException(new IdentityDomainError.InvalidAccessCode("accessContext", "invalid"), "Access context is not active"));

        IdentityAuthenticationException exception = assertThrows(
                IdentityAuthenticationException.class,
                () -> resolver.resolve(principal)
        );

        assertInstanceOf(IdentitySecurityError.InvalidAccessContext.class, exception.getMessageSource());
    }
}

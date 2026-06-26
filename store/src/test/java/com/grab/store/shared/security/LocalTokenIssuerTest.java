package com.grab.store.shared.security;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.repository.RefreshSessionStore;
import com.identity.domain.service.TokenPair;
import com.identity.domain.valueobject.RefreshSessionDetails;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalTokenIssuerTest {

    @Mock
    private RefreshSessionStore sessions;
    @Mock
    private PlatformIdentityResolver identityResolver;

    private KeyPair keyPair;
    private LocalJwtProperties properties;
    private LocalTokenIssuer tokenIssuer;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        keyPair = kpg.generateKeyPair();

        properties = new LocalJwtProperties("test-issuer", "test-audience", Duration.ofMinutes(15), Duration.ofDays(7));
        tokenIssuer = new LocalTokenIssuer(keyPair.getPrivate(), properties, sessions, identityResolver);
    }

    @Test
    void issue_withActiveUser_shouldCreateRs256AccessAndOpaqueRefreshTokens() {
        String userId = UUID.randomUUID().toString();
        AuthenticatedActor actor = new AuthenticatedActor(userId, properties.issuer(), userId, "test@example.com", Set.of("CUSTOMER"), Set.of());

        TokenPair tokenPair = tokenIssuer.issue(actor);

        assertNotNull(tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());
        assertTrue(tokenPair.expiresInMs() > 0);

        var parsed = Jwts.parser().verifyWith(keyPair.getPublic()).build().parseSignedClaims(tokenPair.accessToken());
        assertEquals("at+jwt", parsed.getHeader().getType());
        assertEquals("test-issuer", parsed.getPayload().getIssuer());
        assertEquals(userId, parsed.getPayload().getSubject());
        assertEquals("test@example.com", parsed.getPayload().get("email"));

        verify(sessions).saveNewSession(eq(userId), anyString(), anyString(), any(Instant.class));
    }

    @Test
    void refresh_withReusedToken_shouldRevokeTokenFamily() {
        String tokenFamily = UUID.randomUUID().toString();

        RefreshSessionDetails oldSession = new RefreshSessionDetails(
                "userId1", "test@example.com", tokenFamily, Instant.now().plusSeconds(3600), Instant.now().minusSeconds(10));
        when(sessions.findByTokenHash(anyString())).thenReturn(Optional.of(oldSession));

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> tokenIssuer.refresh("some-refresh-token"));
        assertInstanceOf(IdentitySecurityError.InvalidRefreshToken.class, exception.getMessageSource());

        verify(sessions).revokeFamily(tokenFamily);
    }

    @Test
    void refresh_withExpiredToken_shouldFail() {
        RefreshSessionDetails oldSession = new RefreshSessionDetails(
                "userId1", "test@example.com", "tokenFamily", Instant.now().minusSeconds(10), null);

        when(sessions.findByTokenHash(anyString())).thenReturn(Optional.of(oldSession));

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> tokenIssuer.refresh("some-refresh-token"));
        assertInstanceOf(IdentitySecurityError.InvalidRefreshToken.class, exception.getMessageSource());
    }

    @Test
    void refresh_withValidToken_shouldReturnNewTokenPair() {
        String tokenFamily = UUID.randomUUID().toString();
        
        RefreshSessionDetails oldSession = new RefreshSessionDetails(
                "userId1", "test@example.com", tokenFamily, Instant.now().plus(Duration.ofDays(1)), null);

        when(sessions.findByTokenHash(anyString())).thenReturn(Optional.of(oldSession));

        AuthenticatedActor actor = new AuthenticatedActor("userId1", properties.issuer(), "userId1", "test@example.com", Set.of("CUSTOMER"), Set.of());
        when(identityResolver.resolve(any(ExternalPrincipal.class))).thenReturn(actor);

        TokenPair tokenPair = tokenIssuer.refresh("some-refresh-token");

        assertNotNull(tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());

        verify(sessions).replaceSession(anyString(), anyString(), any(Instant.class));
    }

    @Test
    void revoke_shouldRevokeTokenSession() {
        tokenIssuer.revoke("some-refresh-token");

        verify(sessions).revokeSession(anyString());
    }

    @Test
    void revokeAll_shouldRevokeAllSessionsForUser() {
        Id userId = new CommonId("user-1");
        
        tokenIssuer.revokeAll(userId);

        verify(sessions).revokeAll("user-1");
    }
}

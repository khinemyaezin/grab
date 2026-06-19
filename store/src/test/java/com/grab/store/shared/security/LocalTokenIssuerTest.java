package com.grab.store.shared.security;

import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.service.TokenPair;
import com.grab.framework.security.PlatformIdentityResolver;
import com.identity.infrastructure.entity.RefreshSessionEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.RefreshSessionJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalTokenIssuerTest {

    @Mock
    private RefreshSessionJpaRepository sessions;
    @Mock
    private UserJpaRepository users;
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
        tokenIssuer = new LocalTokenIssuer(keyPair.getPrivate(), properties, sessions, users, identityResolver);
    }

    @Test
    void issue_withActiveUser_shouldCreateRs256AccessAndOpaqueRefreshTokens() {
        String userId = UUID.randomUUID().toString();
        AuthenticatedActor actor = new AuthenticatedActor(userId, properties.issuer(), userId, "test@example.com", Set.of("CUSTOMER"), Set.of());

        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(userId);
        userEntity.setEmail("test@example.com");

        when(users.findByUuid(userId)).thenReturn(Optional.of(userEntity));

        TokenPair tokenPair = tokenIssuer.issue(actor);

        assertNotNull(tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());
        assertTrue(tokenPair.expiresInMs() > 0);

        var parsed = Jwts.parser().verifyWith(keyPair.getPublic()).build().parseSignedClaims(tokenPair.accessToken());
        assertEquals("at+jwt", parsed.getHeader().getType());
        assertEquals("test-issuer", parsed.getPayload().getIssuer());
        assertEquals(userId, parsed.getPayload().getSubject());
        assertEquals("test@example.com", parsed.getPayload().get("email"));

        ArgumentCaptor<RefreshSessionEntity> sessionCaptor = ArgumentCaptor.forClass(RefreshSessionEntity.class);
        verify(sessions).save(sessionCaptor.capture());

        RefreshSessionEntity savedSession = sessionCaptor.getValue();
        assertEquals(userEntity, savedSession.getUser());
        assertNotNull(savedSession.getTokenHash());
        assertNotNull(savedSession.getTokenFamilyId());
        assertTrue(savedSession.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void refresh_withReusedToken_shouldRevokeTokenFamily() {
        String tokenFamily = UUID.randomUUID().toString();

        RefreshSessionEntity oldSession = new RefreshSessionEntity();
        oldSession.setTokenFamilyId(tokenFamily);
        oldSession.setRevokedAt(Instant.now().minusSeconds(10));

        when(sessions.findByTokenHash(anyString())).thenReturn(Optional.of(oldSession));

        RefreshSessionEntity familyMember = new RefreshSessionEntity();
        familyMember.setTokenFamilyId(tokenFamily);
        familyMember.setRevokedAt(null);

        when(sessions.findByTokenFamilyId(tokenFamily)).thenReturn(List.of(familyMember));

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> tokenIssuer.refresh("some-refresh-token"));
        assertInstanceOf(IdentitySecurityError.InvalidRefreshToken.class, exception.getMessageSource());

        verify(sessions).saveAll(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<RefreshSessionEntity> entities = (List<RefreshSessionEntity>) list;
            return entities.get(0).getRevokedAt() != null;
        }));
    }

    @Test
    void refresh_withExpiredToken_shouldFail() {
        RefreshSessionEntity oldSession = new RefreshSessionEntity();
        oldSession.setExpiresAt(Instant.now().minusSeconds(10));
        oldSession.setRevokedAt(null);

        when(sessions.findByTokenHash(anyString())).thenReturn(Optional.of(oldSession));

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> tokenIssuer.refresh("some-refresh-token"));
        assertInstanceOf(IdentitySecurityError.InvalidRefreshToken.class, exception.getMessageSource());
    }

    @Test
    void refresh_withValidToken_shouldReturnNewTokenPair() {
        String tokenFamily = UUID.randomUUID().toString();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setEmail("test@example.com");

        RefreshSessionEntity oldSession = new RefreshSessionEntity();
        oldSession.setTokenFamilyId(tokenFamily);
        oldSession.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        oldSession.setRevokedAt(null);
        oldSession.setUser(userEntity);

        RefreshSessionEntity newSession = new RefreshSessionEntity();
        newSession.setId(2L);
        
        when(sessions.findByTokenHash(anyString()))
                .thenReturn(Optional.of(oldSession))
                .thenReturn(Optional.of(newSession));

        AuthenticatedActor actor = new AuthenticatedActor(userEntity.getUuid(), properties.issuer(), userEntity.getUuid(), "test@example.com", Set.of("CUSTOMER"), Set.of());
        when(identityResolver.resolve(any(ExternalPrincipal.class))).thenReturn(actor);
        when(users.findByUuid(anyString())).thenReturn(Optional.of(userEntity));

        TokenPair tokenPair = tokenIssuer.refresh("some-refresh-token");

        assertNotNull(tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());

        verify(sessions, times(2)).save(any(RefreshSessionEntity.class));
        assertNotNull(oldSession.getRevokedAt());
    }

    @Test
    void revoke_shouldRevokeTokenFamily() {
        RefreshSessionEntity session = new RefreshSessionEntity();
        session.setRevokedAt(null);
        when(sessions.findByTokenHash(anyString())).thenReturn(Optional.of(session));

        tokenIssuer.revoke("some-refresh-token");

        verify(sessions).save(argThat(s -> s.getRevokedAt() != null));
    }
}

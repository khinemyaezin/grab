package com.grab.store.shared.security;

import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.AccessContext;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LocalJwtAccessTokenAuthenticatorTest {

    private KeyPair keyPair;
    private LocalJwtAccessTokenAuthenticator authenticator;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        keyPair = kpg.generateKeyPair();

        LocalJwtProperties properties = new LocalJwtProperties("test-issuer", "test-audience", Duration.ofMinutes(15), Duration.ofDays(1));
        authenticator = new LocalJwtAccessTokenAuthenticator(keyPair.getPublic(), properties);
    }

    @Test
    void authenticate_validToken_returnsPrincipal() {
        String token = Jwts.builder()
                .header().type("at+jwt").and()
                .issuer("test-issuer")
                .audience().add("test-audience").and()
                .subject("test-subject")
                .claim("email", "test@example.com")
                .claim("roles", List.of("CUSTOMER", "SELLER"))
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        ExternalPrincipal principal = authenticator.authenticate(token);

        assertEquals("test-issuer", principal.issuer());
        assertEquals("test-subject", principal.subject());
        assertEquals("test@example.com", principal.email().orElse(null));
        assertEquals(Set.of("CUSTOMER", "SELLER"), principal.entitlements());
    }

    @Test
    void authenticate_invalidAudience_throwsException() {
        String token = Jwts.builder()
                .header().type("at+jwt").and()
                .issuer("test-issuer")
                .audience().add("wrong-audience").and()
                .subject("test-subject")
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> authenticator.authenticate(token));
        assertInstanceOf(IdentitySecurityError.InvalidTokenAudience.class, exception.getMessageSource());
    }

    @Test
    void authenticate_invalidTokenType_throwsException() {
        String token = Jwts.builder()
                .header().type("id+jwt").and()
                .issuer("test-issuer")
                .audience().add("test-audience").and()
                .subject("test-subject")
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> authenticator.authenticate(token));
        assertInstanceOf(IdentitySecurityError.InvalidTokenType.class, exception.getMessageSource());
    }

    @Test
    void authenticate_expiredToken_throwsException() {
        String token = Jwts.builder()
                .header().type("at+jwt").and()
                .issuer("test-issuer")
                .audience().add("test-audience").and()
                .subject("test-subject")
                .expiration(Date.from(Instant.now().minus(Duration.ofMinutes(5))))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> authenticator.authenticate(token));
        assertInstanceOf(IdentitySecurityError.TokenExpired.class, exception.getMessageSource());
    }

    @Test
    void authenticate_invalidSignature_throwsException() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair otherKeyPair = kpg.generateKeyPair();

        String token = Jwts.builder()
                .header().type("at+jwt").and()
                .issuer("test-issuer")
                .audience().add("test-audience").and()
                .subject("test-subject")
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(otherKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        IdentityAuthenticationException exception = assertThrows(IdentityAuthenticationException.class, () -> authenticator.authenticate(token));
        assertInstanceOf(IdentitySecurityError.InvalidToken.class, exception.getMessageSource());
    }

    @Test
    void authenticate_withCompleteContextClaims_shouldReturnScopedPrincipal() {
        String token = Jwts.builder()
                .header().type("at+jwt").and()
                .issuer("test-issuer")
                .audience().add("test-audience").and()
                .subject("test-subject")
                .claim("platform", "SELLER_PORTAL")
                .claim("assignment_id", "assignment-1")
                .claim("scope_type", "MERCHANT_ACCOUNT")
                .claim("scope_id", "merchant-1")
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        ExternalPrincipal principal = authenticator.authenticate(token);

        assertEquals(
                new AccessContext("SELLER_PORTAL", "assignment-1", "MERCHANT_ACCOUNT", "merchant-1"),
                principal.accessContext().orElseThrow()
        );
    }

    @Test
    void authenticate_withIncompleteContextClaims_shouldRejectToken() {
        String token = Jwts.builder()
                .header().type("at+jwt").and()
                .issuer("test-issuer")
                .audience().add("test-audience").and()
                .subject("test-subject")
                .claim("platform", "SELLER_PORTAL")
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(5))))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        IdentityAuthenticationException exception = assertThrows(
                IdentityAuthenticationException.class,
                () -> authenticator.authenticate(token)
        );

        assertInstanceOf(IdentitySecurityError.InvalidToken.class, exception.getMessageSource());
    }
}

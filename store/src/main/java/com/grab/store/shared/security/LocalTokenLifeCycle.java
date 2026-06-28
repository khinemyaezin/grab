package com.grab.store.shared.security;

import com.grab.framework.id.Id;
import com.grab.framework.security.*;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.repository.SessionStore;
import com.identity.domain.service.*;
import com.identity.domain.valueobject.SessionDetails;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class LocalTokenLifeCycle implements TokenLifeCycle {
    private final PrivateKey localJwtPrivateKey;
    private final LocalJwtProperties properties;
    private final SessionStore sessions;
    private final PlatformIdentityResolver identityResolver;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional(transactionManager = "identityTransactionManager")
    public TokenPair issue(AuthenticatedActor actor) {
        return issue(actor, UUID.randomUUID().toString());
    }

    private TokenPair issue(AuthenticatedActor actor, String family) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.accessTokenTtl());
        var builder = Jwts.builder()
                .header()
                .keyId("local-current")
                .type("at+jwt")
                .and()
                .issuer(properties.issuer())
                .subject(actor.platformUserId())
                .audience()
                .add(properties.audience())
                .and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .claim("email", actor.email())
                .claim("roles", actor.roles());
        actor.accessContext().ifPresent(context -> builder
                .claim("platform", context.platformCode())
                .claim("assignment_id", context.assignmentId())
                .claim("scope_type", context.scopeType())
                .claim("scope_id", context.scopeId()));
        String access = builder
                .signWith(localJwtPrivateKey, Jwts.SIG.RS256)
                .compact();
        String refresh = randomToken();
        sessions.saveNewSession(
                actor.platformUserId(),
                hash(refresh),
                family,
                now.plus(properties.refreshTokenTtl()),
                actor.accessContext()
        );
        return new TokenPair(access, refresh, properties.accessTokenTtl().toMillis());
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        SessionDetails old = sessions.findByTokenHash(hash(refreshToken))
                .orElseThrow(this::invalidRefresh);
        Instant now = Instant.now();
        if (old.revokedAt() != null) {
            sessions.revokeFamily(old.tokenFamilyId());
            throw invalidRefresh();
        }
        if (old.expiresAt().isBefore(now))
            throw invalidRefresh();

        ExternalPrincipal principal = new ExternalPrincipal(
                properties.issuer(),
                old.userId(),
                Optional.ofNullable(old.userEmail()),
                Set.of(),
                old.accessContext()
        );
        AuthenticatedActor actor = identityResolver.resolve(principal);
        TokenPair replacement = issue(actor, old.tokenFamilyId());
        sessions.replaceSession(hash(refreshToken), hash(replacement.refreshToken()), now);

        return replacement;
    }

    @Override
    public void revoke(String refreshToken) {
        sessions.revokeSession(hash(refreshToken));
    }

    @Override
    public void revokeAll(Id userId) {
        sessions.revokeAll(userId.getValue());
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private IdentityAuthenticationException invalidRefresh() {
        return new IdentityAuthenticationException(new IdentitySecurityError.InvalidRefreshToken(), "Invalid refresh token");
    }
}

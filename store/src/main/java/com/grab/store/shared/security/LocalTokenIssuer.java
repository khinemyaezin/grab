package com.grab.store.shared.security;

import com.grab.framework.security.*;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.service.*;
import com.identity.infrastructure.entity.*;
import com.identity.infrastructure.repository.jpa.*;
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
public class LocalTokenIssuer implements TokenIssuer {
    private final PrivateKey localJwtPrivateKey;
    private final LocalJwtProperties properties;
    private final RefreshSessionJpaRepository sessions;
    private final UserJpaRepository users;
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
        String access = Jwts.builder().header().keyId("local-current").type("at+jwt").and()
                .issuer(properties.issuer()).subject(actor.platformUserId()).audience().add(properties.audience()).and()
                .issuedAt(Date.from(now)).expiration(Date.from(expiry)).id(UUID.randomUUID().toString())
                .claim("email", actor.email()).claim("roles", actor.roles()).signWith(localJwtPrivateKey, Jwts.SIG.RS256).compact();
        String refresh = randomToken();
        RefreshSessionEntity session = new RefreshSessionEntity();
        session.setUser(users.findByUuid(actor.platformUserId()).orElseThrow());
        session.setTokenHash(hash(refresh));
        session.setTokenFamilyId(family);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(properties.refreshTokenTtl()));
        sessions.save(session);
        return new TokenPair(access, refresh, properties.accessTokenTtl().toMillis());
    }

    @Override
    @Transactional(transactionManager = "identityTransactionManager")
    public TokenPair refresh(String refreshToken) {
        RefreshSessionEntity old = sessions.findByTokenHash(hash(refreshToken)).orElseThrow(() -> invalidRefresh());
        Instant now = Instant.now();
        if (old.getRevokedAt() != null) {
            var family = sessions.findByTokenFamilyId(old.getTokenFamilyId());
            family.forEach(member -> {
                if (member.getRevokedAt() == null) member.setRevokedAt(now);
            });
            sessions.saveAll(family);
            throw invalidRefresh();
        }
        if (old.getExpiresAt().isBefore(now)) throw invalidRefresh();
        ExternalPrincipal principal = new ExternalPrincipal(properties.issuer(), old.getUser().getUuid(), Optional.of(old.getUser().getEmail()), Set.of());
        AuthenticatedActor actor = identityResolver.resolve(principal);
        TokenPair replacement = issue(actor, old.getTokenFamilyId());
        RefreshSessionEntity replacementRow = sessions.findByTokenHash(hash(replacement.refreshToken())).orElseThrow();
        old.setRevokedAt(now);
        old.setLastUsedAt(now);
        old.setReplacedById(replacementRow.getId());
        sessions.save(old);
        return replacement;
    }

    @Override
    @Transactional(transactionManager = "identityTransactionManager")
    public void revoke(String refreshToken) {
        sessions.findByTokenHash(hash(refreshToken)).ifPresent(s -> {
            if (s.getRevokedAt() == null) s.setRevokedAt(Instant.now());
            sessions.save(s);
        });
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private IdentityAuthenticationException invalidRefresh() {
        return new IdentityAuthenticationException(new IdentitySecurityError.InvalidRefreshToken(), "Invalid refresh token");
    }
}

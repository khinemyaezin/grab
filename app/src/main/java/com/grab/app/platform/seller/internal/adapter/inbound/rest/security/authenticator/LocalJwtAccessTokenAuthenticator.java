package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.authenticator;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerSecurityProperties;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception.SellerAuthenticationException;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception.SellerSecurityError;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.ExternalPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LocalJwtAccessTokenAuthenticator implements AccessTokenAuthenticator {

    private final JwtParser parser;
    private final SellerSecurityProperties properties;

    public LocalJwtAccessTokenAuthenticator(PublicKey localJwtPublicKey, SellerSecurityProperties properties) {
        this.parser = Jwts.parser()
                .verifyWith(localJwtPublicKey)
                .requireIssuer(properties.getIssuer())
                .build();
        this.properties = properties;
    }

    @Override
    public ExternalPrincipal authenticate(String token) {
        try {
            Jws<Claims> parsed = parser.parseSignedClaims(token);
            Claims claims = parsed.getPayload();

            if (!claims.getAudience().contains(properties.getAudience())) {
                throw new SellerAuthenticationException(
                        new SellerSecurityError.InvalidTokenAudience(),
                        "Invalid token audience"
                );
            }

            if (!"at+jwt".equals(parsed.getHeader().getType())) {
                throw new SellerAuthenticationException(
                        new SellerSecurityError.InvalidTokenType(),
                        "Invalid token type"
                );
            }

            Object rawRoles = claims.get("roles");
            Set<String> roles = rawRoles instanceof Collection<?> values
                    ? values.stream().map(String::valueOf).collect(Collectors.toUnmodifiableSet())
                    : Set.of();

            return new ExternalPrincipal(
                    claims.getIssuer(),
                    claims.getSubject(),
                    claims.get("email", String.class),
                    roles,
                    accessContext(claims),
                    authenticationVersion(claims)
            );
        } catch (ExpiredJwtException ex) {
            throw new SellerAuthenticationException(
                    new SellerSecurityError.TokenExpired(),
                    "Token has expired",
                    ex
            );
        } catch (JwtException | IllegalArgumentException ex) {
            throw new SellerAuthenticationException(
                    new SellerSecurityError.InvalidToken(),
                    "Invalid access token",
                    ex
            );
        }
    }

    private Long authenticationVersion(Claims claims) {
        Object rawVersion = claims.get("authentication_version");
        if (rawVersion == null) {
            return 0L;
        }
        if (rawVersion instanceof Number version && version.longValue() >= 0) {
            return version.longValue();
        }
        throw new SellerAuthenticationException(
                new SellerSecurityError.InvalidToken(),
                "Authentication version claim is invalid"
        );
    }

    private AccessContext accessContext(Claims claims) {
        String assignmentId = claims.get("assignment_id", String.class);
        String scopeKey = claims.get("scope_key", String.class);
        String scopeId = claims.get("scope_id", String.class);
        if (assignmentId == null && scopeKey == null && scopeId == null) {
            return null;
        }
        if (assignmentId == null || scopeKey == null || scopeId == null) {
            throw new SellerAuthenticationException(
                    new SellerSecurityError.InvalidToken(),
                    "Access context claims are incomplete"
            );
        }
        return new AccessContext(assignmentId, scopeKey, scopeId);
    }
}


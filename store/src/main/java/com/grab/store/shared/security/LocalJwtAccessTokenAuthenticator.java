package com.grab.store.shared.security;

import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LocalJwtAccessTokenAuthenticator implements AccessTokenAuthenticator {
    private final JwtParser parser;
    private final LocalJwtProperties properties;

    public LocalJwtAccessTokenAuthenticator(PublicKey localJwtPublicKey, LocalJwtProperties properties) {
        this.parser = Jwts.parser().verifyWith(localJwtPublicKey).requireIssuer(properties.issuer()).build();
        this.properties = properties;
    }

    @Override
    public ExternalPrincipal authenticate(String token) {
        try {
            Jws<Claims> parsed = parser.parseSignedClaims(token);
            Claims claims = parsed.getPayload();

            if (!claims.getAudience().contains(properties.audience()))
                throw new IdentityAuthenticationException(new IdentitySecurityError.InvalidTokenAudience(), "Invalid token audience");

            if (!"at+jwt".equals(parsed.getHeader().getType()))
                throw new IdentityAuthenticationException(new IdentitySecurityError.InvalidTokenType(), "Invalid token type");

            Object rawRoles = claims.get("roles");
            Set<String> roles = rawRoles instanceof Collection<?> values
                    ? values.stream().map(String::valueOf)
                    .collect(Collectors.toUnmodifiableSet())
                    : Set.of();

            return new ExternalPrincipal(claims.getIssuer(),
                    claims.getSubject(),
                    Optional.ofNullable(claims.get("email", String.class)),
                    roles,
                    accessContext(claims));
        } catch (ExpiredJwtException ex) {
            throw new IdentityAuthenticationException(new IdentitySecurityError.TokenExpired(), "Token has expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IdentityAuthenticationException(new IdentitySecurityError.InvalidToken(), "Invalid access token", ex);
        }
    }

    private Optional<AccessContext> accessContext(Claims claims) {
        String platform = claims.get("platform", String.class);
        String assignmentId = claims.get("assignment_id", String.class);
        String scopeType = claims.get("scope_type", String.class);
        String scopeId = claims.get("scope_id", String.class);
        if (platform == null && assignmentId == null && scopeType == null && scopeId == null) {
            return Optional.empty();
        }
        if (platform == null || assignmentId == null || scopeType == null || scopeId == null) {
            throw new IdentityAuthenticationException(
                    new IdentitySecurityError.InvalidToken(),
                    "Access context claims are incomplete"
            );
        }
        return Optional.of(new AccessContext(platform, assignmentId, scopeType, scopeId));
    }
}

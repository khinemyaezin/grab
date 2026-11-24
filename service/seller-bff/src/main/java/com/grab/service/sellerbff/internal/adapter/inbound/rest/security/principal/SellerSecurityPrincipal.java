package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.principal;

import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.contract.message.account.scim.GrabOidcClaims;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.*;

public record SellerSecurityPrincipal(
        AuthenticatedActor actor,
        OidcUser oidcUser
) implements OidcUser, UserDetails, java.io.Serializable {

    public SellerSecurityPrincipal(AuthenticatedActor actor) {
        this(actor, null);
    }

    public static SellerSecurityPrincipal from(OidcUser oidcUser, String issuer) {
        Map<String, Object> claims = oidcUser.getClaims() != null ? oidcUser.getClaims() : Map.of();
        String subject = oidcUser.getSubject() != null ? oidcUser.getSubject() : oidcUser.getName();
        String platformUserId = (String) claims.get(GrabOidcClaims.PLATFORM_USER_ID);
        if (platformUserId == null || platformUserId.isBlank()) {
            platformUserId = subject;
        }
        String email = (String) claims.get(GrabOidcClaims.EMAIL);
        if (email == null || email.isBlank()) {
            email = oidcUser.getName() != null ? oidcUser.getName() : subject;
        }

        String assignmentId = (String) claims.get(GrabOidcClaims.ASSIGNMENT_ID);
        String scopeKey = (String) claims.get(GrabOidcClaims.SCOPE_KEY);
        String scopeId = (String) claims.get(GrabOidcClaims.SCOPE_ID);
        String role = (String) claims.get(GrabOidcClaims.ROLE);

        Set<String> roles = role != null && !role.isBlank() ? Set.of(role) : Set.of();

        Set<String> authorities = new HashSet<>();
        Object authClaim = claims.get(GrabOidcClaims.AUTHORITIES);
        if (authClaim instanceof Collection<?> authCol) {
            for (Object a : authCol) {
                if (a != null) authorities.add(a.toString());
            }
        }
        if (oidcUser.getAuthorities() != null) {
            for (GrantedAuthority ga : oidcUser.getAuthorities()) {
                if (ga != null && ga.getAuthority() != null) {
                    authorities.add(ga.getAuthority());
                }
            }
        }

        AccessContext accessContext = (scopeKey != null && !scopeKey.isBlank() && scopeId != null && !scopeId.isBlank())
                ? new AccessContext(assignmentId, scopeKey, scopeId)
                : null;

        AuthenticatedActor actor = new AuthenticatedActor(
                platformUserId,
                issuer != null ? issuer : "grab-iam",
                subject,
                email,
                roles,
                authorities,
                accessContext,
                0L
        );

        return new SellerSecurityPrincipal(actor, oidcUser);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return actor.authorities().stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return actor.email();
    }

    @Override
    public String getName() {
        return oidcUser != null ? oidcUser.getName() : actor.email();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser != null ? oidcUser.getAttributes() : Map.of();
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser != null ? oidcUser.getClaims() : Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser != null ? oidcUser.getUserInfo() : null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser != null ? oidcUser.getIdToken() : null;
    }

    public String getPlatformUserId() {
        return actor.platformUserId();
    }

    public Optional<AccessContext> getAccessContext() {
        return Optional.ofNullable(actor.accessContext());
    }

    public Optional<String> getScopeKey() {
        return getAccessContext().map(AccessContext::scopeKey);
    }

    public Optional<String> getScopeId() {
        return getAccessContext().map(AccessContext::scopeId);
    }

    public String requiredScopeId() {
        return getScopeId()
                .filter(scopeId -> !scopeId.isBlank())
                .orElseThrow(() -> new AccessDeniedException("Authenticated seller has no active merchant scope"));
    }

    public String requiredPlatformUserId() {
        String platformUserId = getPlatformUserId();
        if (platformUserId == null || platformUserId.isBlank()) {
            throw new AccessDeniedException("Authenticated seller has no platform user identity");
        }
        return platformUserId;
    }

    public boolean isOnboarding() {
        if (oidcUser != null && oidcUser.getClaims() != null) {
            Object onboarding = oidcUser.getClaims().get(GrabOidcClaims.ONBOARDING_SCOPE);
            return "true".equalsIgnoreCase(String.valueOf(onboarding));
        }
        return actor.accessContext() == null;
    }
}

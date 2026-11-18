package com.grab.service.authorizationserver.core.introspection;

import com.grab.service.authorizationserver.config.AuthorizationIssuerProperties;
import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport;
import com.grab.service.authorizationserver.core.grant.AuthorizationIdentity;
import com.grab.service.authorizationserver.core.grant.AuthorizationIdentity.Identity;
import com.grab.service.authorizationserver.port.outbound.AssignmentIdentityPort.State;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenIntrospectionAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationTokenIntrospectionProvider implements AuthenticationProvider {
    private final OAuth2AuthorizationService authorizations;
    private final AuthorizationIdentity identities;
    private final AuthorizationIssuerProperties properties;
    private final AuthorizationClientPolicies clients;

    public AuthorizationTokenIntrospectionProvider(OAuth2AuthorizationService authorizations,
                                            AuthorizationIdentity identities,
                                            AuthorizationIssuerProperties properties,
                                            AuthorizationClientPolicies clients) {
        this.authorizations = authorizations;
        this.identities = identities;
        this.properties = properties;
        this.clients = clients;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        OAuth2TokenIntrospectionAuthenticationToken request = (OAuth2TokenIntrospectionAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken client = AuthorizationTokenSupport.validateClient(request.getPrincipal(), clients);
        String clientId = client.getRegisteredClient().getClientId();
        String audience = clients.find(clientId).orElseThrow().audience();
        String supplied = request.getToken();
        OAuth2Authorization authorization = authorizations.findByToken(supplied, OAuth2TokenType.ACCESS_TOKEN);
        OAuth2TokenIntrospection.Builder claims = OAuth2TokenIntrospection.builder();
        if (AuthorizationTokenSupport.owns(authorization, client)
                && authorization.getAccessToken() != null
                && authorization.getAccessToken().isActive()
                && AuthorizationTokenSupport.hasAudience(authorization, audience)) {
            Identity identity = identities.live(authorization, clientId);
            if (identity != null) {
                OAuth2AccessToken access = authorization.getAccessToken().getToken();
                claims.active(true)
                        .clientId(clientId)
                        .issuer(properties.issuer()).audience(audience)
                        .subject(identity.subject().userId()).issuedAt(access.getIssuedAt()).expiresAt(access.getExpiresAt())
                        .tokenType("Bearer").claim(AuthorizationTokenSupport.VERSION, identity.subject().authenticationVersion());
                State state = identity.assignment();
                if (state != null) {
                    claims.claim("email", state.email()).claim("roles", state.roles()).claim("authorities", state.authorities());
                    if (state.assignmentId() != null) {
                        claims.claim(AuthorizationTokenSupport.ASSIGNMENT, state.assignmentId())
                                .claim(AuthorizationTokenSupport.SCOPE_KEY, state.scopeKey())
                                .claim(AuthorizationTokenSupport.SCOPE_ID, state.scopeId());
                    }
                }
            }
        }
        return new OAuth2TokenIntrospectionAuthenticationToken(supplied, client, claims.build());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2TokenIntrospectionAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

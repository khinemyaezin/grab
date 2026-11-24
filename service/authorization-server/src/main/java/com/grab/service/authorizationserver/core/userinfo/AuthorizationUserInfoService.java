package com.grab.service.authorizationserver.core.userinfo;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.config.AuthorizationClientProperties.Client;
import com.grab.service.authorizationserver.core.grant.AuthorizationIdentity;
import com.grab.service.authorizationserver.core.grant.AuthorizationIdentity.Identity;
import com.grab.service.authorizationserver.core.grant.AuthorizationTokenSupport;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.core.user.SelectedContext;
import com.grab.service.contract.message.account.scim.GrabOidcClaims;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.function.Function;

@Service
public class AuthorizationUserInfoService implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {
    private final RegisteredClientRepository registrations;
    private final AuthorizationClientPolicies policies;
    private final AuthorizationIdentity identities;
    private final IamUserRepository users;

    public AuthorizationUserInfoService(RegisteredClientRepository registrations,
                                        AuthorizationClientPolicies policies,
                                        AuthorizationIdentity identities,
                                        IamUserRepository users) {
        this.registrations = registrations;
        this.policies = policies;
        this.identities = identities;
        this.users = users;
    }

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        RegisteredClient client = registrations.findById(authorization.getRegisteredClientId());
        if (client == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        String clientId = client.getClientId();
        Client policy = policies.find(clientId).orElseThrow();
        if (!AuthorizationTokenSupport.hasAudience(authorization, policy.audience())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        Identity identity = identities.live(authorization);
        if (identity == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }

        OidcUserInfo.Builder builder = OidcUserInfo.builder().subject(identity.subject().userId());

        SelectedContext selected = SelectedContext.from(authorization);
        if (selected != null) {
            if (selected.onboarding()) {
                builder.claim(GrabOidcClaims.ONBOARDING_SCOPE, "true");
                if (selected.platformUserId() != null) {
                    builder.claim(GrabOidcClaims.PLATFORM_USER_ID, selected.platformUserId());
                }
            } else {
                if (selected.platformUserId() != null) builder.claim(GrabOidcClaims.PLATFORM_USER_ID, selected.platformUserId());
                if (selected.assignmentId() != null) builder.claim(GrabOidcClaims.ASSIGNMENT_ID, selected.assignmentId());
                if (selected.scopeKey() != null) builder.claim(GrabOidcClaims.SCOPE_KEY, selected.scopeKey());
                if (selected.scopeId() != null) builder.claim(GrabOidcClaims.SCOPE_ID, selected.scopeId());
                if (selected.role() != null) builder.claim(GrabOidcClaims.ROLE, selected.role());
                if (selected.authorities() != null && !selected.authorities().isEmpty()) {
                    builder.claim(GrabOidcClaims.AUTHORITIES, new ArrayList<>(selected.authorities()));
                }
            }
        }

        if (users != null) {
            users.findById(identity.subject().userId()).ifPresent(user -> {
                builder.claim(GrabOidcClaims.EMAIL, user.email());
                builder.claim("email_verified", true);
                if (user.firstName() != null) builder.claim("given_name", user.firstName());
                if (user.lastName() != null) builder.claim("family_name", user.lastName());
            });
        }

        return builder.build();
    }
}

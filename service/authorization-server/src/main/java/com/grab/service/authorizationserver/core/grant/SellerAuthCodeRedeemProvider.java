package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.adapter.inbound.web.SellerFormAuthenticationProvider;
import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import java.security.Principal;

public class SellerAuthCodeRedeemProvider implements AuthenticationProvider {
    private static final OAuth2TokenType CODE = new OAuth2TokenType(OAuth2ParameterNames.CODE);

    private final AuthenticationProvider delegate;
    private final OAuth2AuthorizationService authorizations;
    private final GrantFamilyStorePort store;
    private final AccountIdentityPort account;
    private final SellerIssuerProperties properties;
    private final MeterRegistry metrics;

    public SellerAuthCodeRedeemProvider(AuthenticationProvider delegate,
                                        OAuth2AuthorizationService authorizations,
                                        GrantFamilyStorePort store,
                                        AccountIdentityPort account,
                                        SellerIssuerProperties properties,
                                        MeterRegistry metrics) {
        this.delegate = delegate;
        this.authorizations = authorizations;
        this.store = store;
        this.account = account;
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        OAuth2AuthorizationCodeAuthenticationToken grant = (OAuth2AuthorizationCodeAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken client = SellerTokenSupport.validateClient(grant.getPrincipal(), properties.clientId());
        Authentication result = store.inTransaction(() -> {
            OAuth2Authorization authorization = SellerTokenSupport.lockCurrent(
                    authorizations, store, grant.getCode(), CODE, client);
            if (authorization == null) {
                return null;
            }
            OAuth2Authorization.Token<OAuth2AuthorizationCode> code = authorization.getToken(OAuth2AuthorizationCode.class);
            if (code == null || !code.isActive()) {
                SellerTokenSupport.invalidate(authorizations, authorization);
                return null;
            }
            Authentication principal = authorization.getAttribute(Principal.class.getName());
            AuthenticationState state = SellerTokenSupport.resolve(account, metrics, authorization.getPrincipalName(), null, true);
            if (state == null || state.authenticationVersion() != SellerFormAuthenticationProvider.loginVersion(principal)) {
                return null;
            }
            OAuth2Authorization contextual = SellerTokenSupport.withState(authorization, state);
            authorizations.save(contextual);
            return delegate.authenticate(grant);
        });

        if (result == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2AuthorizationCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

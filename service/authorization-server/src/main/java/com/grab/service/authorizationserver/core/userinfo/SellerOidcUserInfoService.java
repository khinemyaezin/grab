package com.grab.service.authorizationserver.core.userinfo;

import com.grab.service.authorizationserver.core.grant.SellerTokenSupport;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class SellerOidcUserInfoService implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {
    private final AccountIdentityPort account;
    private final MeterRegistry metrics;

    public SellerOidcUserInfoService(AccountIdentityPort account, MeterRegistry metrics) {
        this.account = account;
        this.metrics = metrics;
    }

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        AuthenticationState state = SellerTokenSupport.liveState(authorization, account, metrics);
        if (state == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        return OidcUserInfo.builder().subject(state.userId()).build();
    }
}

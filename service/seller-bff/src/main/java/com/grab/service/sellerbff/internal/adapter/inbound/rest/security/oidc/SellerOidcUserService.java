package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class SellerOidcUserService extends OidcUserService {
    private final SellerOidcProperties properties;

    public SellerOidcUserService(SellerOidcProperties properties) {
        this.properties = properties;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        return SellerSecurityPrincipal.from(oidcUser, properties.issuer());
    }
}

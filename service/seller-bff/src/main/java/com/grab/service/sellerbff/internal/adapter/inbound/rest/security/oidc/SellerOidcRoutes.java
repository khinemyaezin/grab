package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

final class SellerOidcRoutes {
    static final String REGISTRATION_ID = "seller";
    static final String AUTHORIZATION_BASE = "/oauth2/authorization";
    static final String LOGIN = AUTHORIZATION_BASE + "/" + REGISTRATION_ID;
    static final String CALLBACK = "/login/oauth2/code/" + REGISTRATION_ID;

    private SellerOidcRoutes() {}
}

package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

public final class SellerOidcRoutes {
    public static final String REGISTRATION_ID = "seller";
    public static final String AUTHORIZATION_BASE = "/oauth2/authorization";
    public static final String LOGIN = AUTHORIZATION_BASE + "/" + REGISTRATION_ID;
    public static final String CALLBACK = "/login/oauth2/code/" + REGISTRATION_ID;

    private SellerOidcRoutes() {}
}

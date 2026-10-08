package com.merchant.application.security;

import com.grab.framework.security.ScopeDeclaration;

import java.util.List;

public final class MerchantScopeManifest {
    public static final int VERSION = 1;
    public static final String ACCOUNT_SCOPE_KEY = "merchant.account";
    public static final String STOREFRONT_SCOPE_KEY = "merchant.storefront";

    public static final List<ScopeDeclaration> SCOPES = List.of(
            new ScopeDeclaration(ACCOUNT_SCOPE_KEY, null),
            new ScopeDeclaration(STOREFRONT_SCOPE_KEY, ACCOUNT_SCOPE_KEY)
    );

    private MerchantScopeManifest() {
    }
}

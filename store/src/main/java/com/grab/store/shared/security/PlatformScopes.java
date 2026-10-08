package com.grab.store.shared.security;

import com.inventory.application.security.InventoryScopeManifest;
import com.merchant.application.security.MerchantScopeManifest;

public class PlatformScopes {
    public static final String MERCHANT_ACCOUNT_SCOPE = MerchantScopeManifest.ACCOUNT_SCOPE_KEY;
    public static final String MERCHANT_STOREFRONT_SCOPE = MerchantScopeManifest.STOREFRONT_SCOPE_KEY;
    public static final String FULFILLMENT_LOCATION_SCOPE = InventoryScopeManifest.FULFILLMENT_LOCATION_SCOPE_KEY;
    
    private PlatformScopes() {
    }
}

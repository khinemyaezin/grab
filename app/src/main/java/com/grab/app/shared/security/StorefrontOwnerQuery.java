package com.grab.app.shared.security;

public interface StorefrontOwnerQuery {
    boolean belongsToMerchant(String storefrontId, String merchantId);
}

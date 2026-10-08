package com.grab.store.shared.security;

public interface StorefrontOwnerQuery {
    boolean belongsToMerchant(String storefrontId, String merchantId);
}

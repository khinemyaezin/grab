package com.grab.store.shared.security;

public interface LocationOwnerQuery {
    boolean belongsToMerchant(String locationId, String merchantId);
}

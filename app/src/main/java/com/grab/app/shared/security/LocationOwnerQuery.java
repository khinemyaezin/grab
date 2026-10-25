package com.grab.app.shared.security;

public interface LocationOwnerQuery {
    boolean belongsToMerchant(String locationId, String merchantId);
}

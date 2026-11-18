package com.grab.service.account.merchant.port;

import com.grab.framework.id.Id;

public interface SellerScopeRevisionQuery {
    Revision find(String scopeKey, Id scopeId);

    record Revision(long merchantRevision, long storefrontRevision) {}
}

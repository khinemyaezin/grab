package com.identity.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.Optional;

public interface SellerScopeLifecyclePort {
    Optional<ScopeRevision> find(String scopeKey, Id scopeId);

    record ScopeRevision(long merchantRevision, long storefrontRevision) {}
}

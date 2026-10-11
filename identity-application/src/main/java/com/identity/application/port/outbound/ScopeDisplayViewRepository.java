package com.identity.application.port.outbound;

import com.grab.framework.id.Id;

public interface ScopeDisplayViewRepository {
    void update(String scopeKey, Id scopeId, String title, String subtitle, long sourceRevision);
}

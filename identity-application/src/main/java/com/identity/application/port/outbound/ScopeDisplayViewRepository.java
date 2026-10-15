package com.identity.application.port.outbound;

import com.grab.framework.id.Id;

public interface ScopeDisplayViewRepository {
    void update(Id scopeId, String name);
}

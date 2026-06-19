package com.identity.domain.service;

import com.grab.framework.id.Id;

public interface RefreshSessionRevoker {
    void revokeAll(Id userId);
}

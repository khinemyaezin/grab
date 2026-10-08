package com.grab.framework.security;

import com.grab.framework.domain.Event;

import java.util.List;

public interface ScopeManifestDeclaredIntegrationEvent extends Event {
    String moduleKey();

    int manifestVersion();

    List<ScopeDeclaration> scopes();
}

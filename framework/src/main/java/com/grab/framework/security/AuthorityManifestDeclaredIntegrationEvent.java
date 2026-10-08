package com.grab.framework.security;

import com.grab.framework.domain.Event;

import java.util.List;

public interface AuthorityManifestDeclaredIntegrationEvent extends Event {
    String moduleKey();

    int manifestVersion();

    List<AuthorityDefinition> authorities();
}

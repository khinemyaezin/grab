package com.grab.framework.security.role;

import com.grab.framework.domain.Event;
import java.time.Instant;

public interface RoleDeclarationDeclaredIntegrationEvent extends Event {
    String owner();

    String roleCode();

    int declarationRevision();

    RoleDeclaration declaration();

    String eventId();

    default String suppliedContentDigest() {
        return declaration().contentDigest();
    }

    default Instant publishedAt() {
        return null;
    }
}

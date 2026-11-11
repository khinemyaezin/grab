package com.manifest.adapter.persistence;

import com.grab.framework.domain.Event;
import com.grab.framework.security.role.RoleDeclarationPublicationEnvelope;

@FunctionalInterface
public interface RoleDeclarationEventFactory {
    Event create(RoleDeclarationPublicationEnvelope envelope);
}


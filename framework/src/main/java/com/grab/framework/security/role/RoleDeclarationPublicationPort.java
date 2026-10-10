package com.grab.framework.security.role;

public interface RoleDeclarationPublicationPort {
    PublicationResult enqueue(RoleDeclaration declaration);

    enum PublicationResult {
        ENQUEUED,
        NOT_DUE,
        SUPERSEDED,
        CONFLICT
    }
}

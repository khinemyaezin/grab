package com.identity.application.model.write;

public record RegisterRoleDeclarationResult(
        String roleCode,
        String owner,
        int declarationRevision,
        String outcome,
        String reason
) {
}

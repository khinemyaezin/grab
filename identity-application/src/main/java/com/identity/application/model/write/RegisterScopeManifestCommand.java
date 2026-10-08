package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.security.ScopeDeclaration;

import java.util.List;
import java.util.Objects;

public record RegisterScopeManifestCommand(
        String moduleKey,
        int manifestVersion,
        List<ScopeDeclaration> scopes
) implements Command<Void> {
    public RegisterScopeManifestCommand {
        Objects.requireNonNull(moduleKey, "module key is required");
        Objects.requireNonNull(scopes, "scope declarations are required");
        scopes = List.copyOf(scopes);
    }
}

package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.security.AuthorityDefinition;

import java.util.List;

public record RegisterAuthorityManifestCommand(
        String moduleKey,
        int manifestVersion,
        List<AuthorityDefinition> authorities
) implements Command<Void> {
    public RegisterAuthorityManifestCommand {
        authorities = List.copyOf(authorities);
    }
}

package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record EnsureSecurityCatalogStateCommand(boolean creationAllowed)
        implements Command<EnsureSecurityCatalogStateResult> {
}

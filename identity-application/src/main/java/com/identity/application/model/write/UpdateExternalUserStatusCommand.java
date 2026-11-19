package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record UpdateExternalUserStatusCommand(
        String idOrExternalId,
        boolean active
) implements Command<ExternalUserProvisionResult> {
}

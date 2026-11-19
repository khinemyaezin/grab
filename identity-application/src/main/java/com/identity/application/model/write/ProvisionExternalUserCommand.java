package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record ProvisionExternalUserCommand(
        String externalId,
        String email,
        String givenName,
        String familyName
) implements Command<ExternalUserProvisionResult> {
}

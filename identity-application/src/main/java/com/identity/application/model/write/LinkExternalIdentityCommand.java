package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record LinkExternalIdentityCommand(
        Id userId,
        String issuer,
        String subject,
        String directoryId,
        String resourceId,
        String actorId
) implements Command<ExternalIdentityLinkResult> {
}

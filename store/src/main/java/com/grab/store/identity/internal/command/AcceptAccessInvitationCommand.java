package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record AcceptAccessInvitationCommand(
        String acceptanceToken,
        Id userId,
        String userEmail
) implements Command<AccessAssignmentResult> {
}

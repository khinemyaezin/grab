package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record SwitchAccessContextCommand(
        Id userId,
        Id assignmentId,
        String currentRefreshToken
) implements Command<AuthResult> {
}

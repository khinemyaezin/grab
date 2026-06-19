package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record AssignRoleCommand(
        Id userId,
        String roleCode,
        boolean assign
) implements Command<UserProfileResult> {
}

package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;

public record RegisterCommand(
        String email,
        String password
) implements Command<UserProfileResult> {
}

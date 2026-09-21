package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record RegisterCommand(
        String email,
        String password,
        String platformCode
) implements Command<UserProfileResult> {
}

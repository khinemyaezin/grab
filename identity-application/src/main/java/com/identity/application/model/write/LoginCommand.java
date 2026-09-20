package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record LoginCommand(
        String email,
        String password,
        String platformCode
) implements Command<AuthResult> {
}

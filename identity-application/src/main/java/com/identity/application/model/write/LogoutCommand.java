package com.identity.application.model.write;
import com.grab.framework.cqrs.command.Command;

public record LogoutCommand(
        String refreshToken
) implements Command<Void> {}

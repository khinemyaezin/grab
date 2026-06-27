package com.grab.store.identity.internal.command;
import com.grab.framework.cqrs.command.Command;

public record LogoutCommand(
        String refreshToken
) implements Command<Void> {}

package com.grab.store.identity.internal.command;
import com.grab.framework.cqrs.command.Command;
public record RefreshTokenCommand(String refreshToken) implements Command<AuthResult> {}

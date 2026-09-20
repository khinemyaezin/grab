package com.identity.application.model.write;
import com.grab.framework.cqrs.command.Command;
public record RefreshTokenCommand(String refreshToken) implements Command<AuthResult> {}

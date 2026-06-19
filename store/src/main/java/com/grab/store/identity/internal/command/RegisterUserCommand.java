package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;

public record RegisterUserCommand(String email, String password, String role) implements Command<AuthResult> {}

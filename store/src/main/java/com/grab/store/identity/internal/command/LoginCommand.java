package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;

public record LoginCommand(String email, String password) implements Command<AuthResult> {}

package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;

public record AssignRoleCommand(String userId, String roleCode, boolean assign) implements Command<UserProfileResult> {}

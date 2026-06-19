package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;

public record CreateRoleCommand(String code, String name, String description) implements Command<RoleResult> {
}

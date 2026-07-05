package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;

import java.util.Set;

public record CreateRoleCommand(
        String code,
        String name,
        String description,
        String platformCode,
        Set<String> authorityCodes
) implements Command<RoleResult> {
}

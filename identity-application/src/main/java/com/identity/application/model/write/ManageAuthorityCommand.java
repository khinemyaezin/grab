package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record ManageAuthorityCommand(String roleCode, String authorityCode, boolean assign) implements Command<RoleResult> {}

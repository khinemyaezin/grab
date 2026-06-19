package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.identity.domain.enums.UserStatus;

public record ChangeUserStatusCommand(String userId, UserStatus status) implements Command<UserProfileResult> {}

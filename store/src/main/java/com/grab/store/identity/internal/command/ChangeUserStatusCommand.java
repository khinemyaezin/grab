package com.grab.store.identity.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.identity.domain.enums.UserStatus;

public record ChangeUserStatusCommand(
        Id userId,
        UserStatus status
) implements Command<UserProfileResult> {
}

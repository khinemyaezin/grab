package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.ChangeUserStatusCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.enums.UserStatus;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.RefreshSessionJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChangeUserStatusCommandHandler implements CommandHandler<ChangeUserStatusCommand, UserProfileResult> {
    private final UserJpaRepository users;
    private final RefreshSessionJpaRepository sessions;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(ChangeUserStatusCommand command) {
        UserEntity u = users.findByUuid(command.userId())
                .orElseThrow(() -> new IdentityServiceException(new IdentityServiceError.UserNotFound(command.userId()), "User not found"));
        u.setStatus(command.status());
        if (command.status() == UserStatus.SUSPENDED) {
            sessions.findByUser_Uuid(command.userId()).forEach(s -> s.setRevokedAt(java.time.Instant.now()));
        }
        u = users.save(u);
        return new UserProfileResult(u.getUuid(), u.getEmail(), u.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet()), u.getStatus().name(), u.getCreatedAt());
    }

    @Override
    public Class<ChangeUserStatusCommand> getCommandType() {
        return ChangeUserStatusCommand.class;
    }
}

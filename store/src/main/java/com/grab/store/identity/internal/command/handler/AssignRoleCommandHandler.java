package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.AssignRoleCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AssignRoleCommandHandler implements CommandHandler<AssignRoleCommand, UserProfileResult> {
    private final UserJpaRepository users;
    private final RoleJpaRepository roles;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(AssignRoleCommand command) {
        UserEntity u = users.findByUuid(command.userId())
                .orElseThrow(() -> new IdentityServiceException(new IdentityServiceError.UserNotFound(command.userId()), "User not found"));
        RoleEntity r = roles.findByCode(command.roleCode())
                .orElseThrow(() -> new IdentityServiceException(new IdentityServiceError.RoleNotFound(command.roleCode()), "Role not found"));
        
        if (command.assign()) u.getRoles().add(r);
        else u.getRoles().remove(r);
        
        u = users.save(u);
        return new UserProfileResult(u.getUuid(), u.getEmail(), u.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet()), u.getStatus().name(), u.getCreatedAt());
    }

    @Override
    public Class<AssignRoleCommand> getCommandType() {
        return AssignRoleCommand.class;
    }
}

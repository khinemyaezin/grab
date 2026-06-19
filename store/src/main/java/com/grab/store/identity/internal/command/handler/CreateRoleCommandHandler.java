package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.CreateRoleCommand;
import com.grab.store.identity.internal.command.RoleResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreateRoleCommandHandler implements CommandHandler<CreateRoleCommand, RoleResult> {
    private final RoleJpaRepository roles;

    @Override
    @IdentityTransactional
    public RoleResult handle(CreateRoleCommand command) {
        if (roles.findByCode(command.code()).isPresent()) {
            throw new IdentityServiceException(new IdentityServiceError.RoleExists(command.code()), "Role exists");
        }
        RoleEntity r = new RoleEntity();
        r.setUuid(UUID.randomUUID().toString());
        r.setCode(command.code());
        r.setName(command.name());
        r.setDescription(command.description());
        r.setActive(true);
        r = roles.save(r);
        return new RoleResult(r.getCode(), r.getName(), r.getDescription(), r.isActive(), r.getAuthorities().stream().map(AuthorityEntity::getCode).collect(Collectors.toSet()));
    }

    @Override
    public Class<CreateRoleCommand> getCommandType() {
        return CreateRoleCommand.class;
    }
}

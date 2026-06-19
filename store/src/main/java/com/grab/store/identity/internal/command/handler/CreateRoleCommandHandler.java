package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.CreateRoleCommand;
import com.grab.store.identity.internal.command.RoleResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CreateRoleCommandHandler implements CommandHandler<CreateRoleCommand, RoleResult> {

    private final RoleRepository roleRepository;
    private final IdGenerator idGenerator;

    @Override
    @IdentityTransactional
    public RoleResult handle(CreateRoleCommand command) {
        String roleCode = command.code().trim().toUpperCase(Locale.ROOT);
        if (roleRepository.findByCode(roleCode).isPresent()) {
            throw new IdentityServiceException(
                    new IdentityServiceError.RoleExists(roleCode),
                    "Role already exists"
            );
        }

        Role role = Role.create(idGenerator.generateId(), roleCode, command.name(), command.description());
        return toResult(roleRepository.save(role));
    }

    @Override
    public Class<CreateRoleCommand> getCommandType() {
        return CreateRoleCommand.class;
    }

    private RoleResult toResult(Role role) {
        return new RoleResult(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getAuthorityCodes()
        );
    }
}

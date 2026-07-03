package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.CreateRoleCommand;
import com.grab.store.identity.internal.command.RoleResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.service.RoleAdministrationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CreateRoleCommandHandler implements CommandHandler<CreateRoleCommand, RoleResult> {

    private final RoleRepository roleRepository;
    private final PlatformRepository platformRepository;
    private final RoleAdministrationPolicy roleAdministrationPolicy;
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

        Platform platform = platformRepository.findByCode(command.platformCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(command.platformCode()),
                        "Platform not found"
                )
        );
        Role role = roleAdministrationPolicy.createCustomRole(
                idGenerator.generateId(),
                roleCode,
                command.name(),
                command.description(),
                platform,
                command.authorityCodes()
        );
        Role savedRole = roleRepository.save(role);
        platformRepository.save(platform);
        return toResult(savedRole, Set.of(platform.getCode()));
    }

    @Override
    public Class<CreateRoleCommand> getCommandType() {
        return CreateRoleCommand.class;
    }

    private RoleResult toResult(Role role, Set<String> platformCodes) {
        return new RoleResult(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getKind().name(),
                role.isActive(),
                role.isAssignable(),
                role.getAuthorityCodes(),
                platformCodes
        );
    }
}

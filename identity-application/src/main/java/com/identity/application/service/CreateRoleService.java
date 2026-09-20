package com.identity.application.service;

import com.identity.application.port.inbound.CreateRoleUseCase;

import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.RoleResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.port.outbound.PlatformRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Set;

@RequiredArgsConstructor
public class CreateRoleService implements CreateRoleUseCase {

    private final RoleRepository roleRepository;
    private final PlatformRepository platformRepository;
    private final RoleAdministrationPolicy roleAdministrationPolicy;
    private final IdGenerator idGenerator;
    public RoleResult execute(CreateRoleCommand command) {
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

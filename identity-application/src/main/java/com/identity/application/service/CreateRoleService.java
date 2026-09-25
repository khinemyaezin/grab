package com.identity.application.service;

import com.identity.application.port.inbound.CreateRoleUseCase;

import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.RoleResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.Role;
import com.identity.domain.model.Authority;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CreateRoleService implements CreateRoleUseCase {

    private final RoleRepository roleRepository;
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

        Role role = roleAdministrationPolicy.createCustomRole(
                idGenerator.generateId(),
                roleCode,
                command.name(),
                command.description(),
                command.authorityCodes()
        );
        Role savedRole = roleRepository.save(role);
        return toResult(savedRole);
    }

    private RoleResult toResult(Role role) {
        return new RoleResult(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getKind().name(),
                role.isActive(),
                role.isAssignable(),
                role.getAuthorities().stream()
                        .map(Authority::getCode)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }
}

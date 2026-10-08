package com.identity.application.service;

import com.identity.application.port.inbound.ManageAuthorityUseCase;

import com.identity.application.model.write.ManageAuthorityCommand;
import com.identity.application.model.write.RoleResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ManageAuthorityService implements ManageAuthorityUseCase {

    private final RoleRepository roleRepository;
    private final RoleAdministrationPolicy roleAdministrationPolicy;

    public RoleResult execute(ManageAuthorityCommand command) {
        String roleCode = command.roleCode().trim().toUpperCase(Locale.ROOT);
        String authorityCode = command.authorityCode().trim().toUpperCase(Locale.ROOT);

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(roleCode),
                        "Role not found"
                ));

        roleAdministrationPolicy.changeAuthority(role, authorityCode, command.assign());
        return toResult(roleRepository.save(role));
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

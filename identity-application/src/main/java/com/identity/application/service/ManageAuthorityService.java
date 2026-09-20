package com.identity.application.service;

import com.identity.application.port.inbound.ManageAuthorityUseCase;

import com.identity.application.model.write.ManageAuthorityCommand;
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
public class ManageAuthorityService implements ManageAuthorityUseCase {

    private final RoleRepository roleRepository;
    private final PlatformRepository platformRepository;
    private final RoleAdministrationPolicy roleAdministrationPolicy;
    public RoleResult execute(ManageAuthorityCommand command) {
        String roleCode = command.roleCode().trim().toUpperCase(Locale.ROOT);
        String authorityCode = command.authorityCode().trim().toUpperCase(Locale.ROOT);

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(roleCode),
                        "Role not found"
                ));
        Set<Platform> platforms = platformRepository.findByRoleCode(roleCode);
        if (platforms.size() != 1) {
            throw new IdentityServiceException(
                    new IdentityServiceError.RolePlatformBindingInvalid(roleCode),
                    "Custom role must be bound to exactly one platform"
            );
        }
        Platform platform = platforms.iterator().next();
        roleAdministrationPolicy.changeAuthority(role, platform, authorityCode, command.assign());
        return toResult(roleRepository.save(role), Set.of(platform.getCode()));
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

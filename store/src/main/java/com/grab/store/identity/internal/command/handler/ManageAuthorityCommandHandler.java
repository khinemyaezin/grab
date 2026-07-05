package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.ManageAuthorityCommand;
import com.grab.store.identity.internal.command.RoleResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.policy.impl.RoleAdministrationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ManageAuthorityCommandHandler implements CommandHandler<ManageAuthorityCommand, RoleResult> {

    private final RoleRepository roleRepository;
    private final PlatformRepository platformRepository;
    private final RoleAdministrationPolicy roleAdministrationPolicy;

    @Override
    @IdentityTransactional
    public RoleResult handle(ManageAuthorityCommand command) {
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

    @Override
    public Class<ManageAuthorityCommand> getCommandType() {
        return ManageAuthorityCommand.class;
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

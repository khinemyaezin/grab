package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.ManageAuthorityCommand;
import com.grab.store.identity.internal.command.RoleResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.AuthorityRepository;
import com.identity.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ManageAuthorityCommandHandler implements CommandHandler<ManageAuthorityCommand, RoleResult> {

    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

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
        if (!authorityRepository.existsByCode(authorityCode)) {
            throw new IdentityServiceException(
                    new IdentityServiceError.AuthorityNotFound(authorityCode),
                    "Authority not found"
            );
        }

        if (command.assign()) {
            role.assignAuthority(authorityCode);
        } else {
            role.revokeAuthority(authorityCode);
        }

        return toResult(roleRepository.save(role));
    }

    @Override
    public Class<ManageAuthorityCommand> getCommandType() {
        return ManageAuthorityCommand.class;
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

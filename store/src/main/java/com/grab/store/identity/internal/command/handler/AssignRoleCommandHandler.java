package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.AssignRoleCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AssignRoleCommandHandler implements CommandHandler<AssignRoleCommand, UserProfileResult> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(AssignRoleCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(command.userId().getValue()),
                        "User not found"
                ));

        String roleCode = command.roleCode().trim().toUpperCase(Locale.ROOT);
        Role role = roleRepository.findByCode(roleCode)
                .filter(Role::isActive)
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(roleCode),
                        "Active role not found"
                ));

        if (command.assign()) {
            user.assignRole(role.getCode());
        } else {
            user.revokeRole(role.getCode());
        }

        return toResult(userRepository.save(user));
    }

    @Override
    public Class<AssignRoleCommand> getCommandType() {
        return AssignRoleCommand.class;
    }

    private UserProfileResult toResult(User user) {
        return new UserProfileResult(
                user.getId().getValue(),
                user.getEmail().value(),
                user.getRoleCodes(),
                user.getStatus().name(),
                user.getCreatedAt().toString()
        );
    }
}

package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.ChangeUserStatusCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.TokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeUserStatusCommandHandler implements CommandHandler<ChangeUserStatusCommand, UserProfileResult> {

    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(ChangeUserStatusCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(command.userId().getValue()),
                        "User not found"
                ));

        if (command.status() == UserStatus.SUSPENDED) {
            user.suspend();
            tokenIssuer.revokeAll(user.getId());
        } else if (command.status() == UserStatus.ACTIVE && user.getStatus() == UserStatus.PENDING_APPROVAL) {
            user.activate();
        } else if (command.status() == UserStatus.ACTIVE && user.getStatus() == UserStatus.SUSPENDED) {
            user.reactivate();
        } else {
            throw new IdentityServiceException(
                    new IdentityServiceError.InvalidStatusTransition(
                            user.getStatus().name(),
                            command.status().name()
                    ),
                    "Invalid user status transition"
            );
        }

        return toResult(userRepository.save(user));
    }

    @Override
    public Class<ChangeUserStatusCommand> getCommandType() {
        return ChangeUserStatusCommand.class;
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

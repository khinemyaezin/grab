package com.identity.application.service;

import com.identity.application.port.inbound.ChangeUserStatusUseCase;

import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.service.TokenLifeCycle;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangeUserStatusService implements ChangeUserStatusUseCase {

    private final UserRepository userRepository;
    private final TokenLifeCycle tokenLifeCycle;
    public UserProfileResult execute(ChangeUserStatusCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(command.userId().getValue()),
                        "User not found"
                ));

        if (command.status() == UserStatus.SUSPENDED) {
            user.suspend();
            tokenLifeCycle.revokeAll(user.getId());
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
private UserProfileResult toResult(User user) {
        return new UserProfileResult(
                user.getId().getValue(),
                user.getEmail().value(),
                user.getStatus().name(),
                user.getCreatedAt().toString()
        );
    }
}

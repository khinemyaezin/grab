package com.identity.application.service;

import com.identity.application.port.inbound.ChangeUserStatusUseCase;

import com.grab.framework.id.Id;
import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.domain.service.TokenLifeCycle;

import java.util.function.Consumer;

public class ChangeUserStatusService implements ChangeUserStatusUseCase {

    private final UserRepository userRepository;
    private final Consumer<Id> revokeSessions;

    public ChangeUserStatusService(UserRepository userRepository, TokenLifeCycle tokenLifeCycle) {
        this(userRepository, tokenLifeCycle::revokeAll);
    }

    private ChangeUserStatusService(UserRepository userRepository, Consumer<Id> revokeSessions) {
        this.userRepository = userRepository;
        this.revokeSessions = revokeSessions;
    }

    public static ChangeUserStatusUseCase forHeadlessAccount(UserRepository users, SessionStore sessions) {
        return new ChangeUserStatusService(users, id -> sessions.revokeAll(id.getValue()));
    }
    public UserProfileResult execute(ChangeUserStatusCommand command) {
        User user = userRepository.findByIdForUpdate(command.userId())
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(command.userId().getValue()),
                        "User not found"
                ));

        if (command.status() == UserStatus.SUSPENDED) {
            user.suspend();
            user.invalidateAuthenticationSessions();
            revokeSessions.accept(command.userId());
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

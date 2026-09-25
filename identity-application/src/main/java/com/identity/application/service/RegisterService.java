package com.identity.application.service;

import com.identity.application.port.inbound.RegisterUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.User;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {
    private final UserRepository users;
    private final AccessAssignmentRepository accessAssignments;
    private final PasswordHasher passwordHasher;
    private final IdGenerator idGenerator;

    public UserProfileResult execute(RegisterCommand command) {
        Email email = new Email(command.email());
        if (users.findByEmail(email).isPresent()) {
            throw new IdentityServiceException(
                    new IdentityServiceError.EmailExists(command.email()),
                    "User with this email already exists"
            );
        }

        Id userId = idGenerator.generateId();
        HashedPassword password = passwordHasher.hash(command.password());
        User user = User.createLocal(userId, email, password);

        User saved = users.save(user);

        return new UserProfileResult(
                saved.getId().getValue(),
                saved.getEmail().value(),
                saved.getStatus().name(),
                saved.getCreatedAt().toString()
        );
    }
}

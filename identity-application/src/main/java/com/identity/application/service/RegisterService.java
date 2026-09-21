package com.identity.application.service;

import com.identity.application.port.inbound.RegisterUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.User;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.PlatformRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.policy.RegistrationAccessPolicy;
import com.identity.domain.policy.RegistrationAccessPolicyResolver;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {
    private final UserRepository users;
    private final PlatformRepository platforms;
    private final AccessAssignmentRepository accessAssignments;
    private final PasswordHasher passwordHasher;
    private final IdGenerator idGenerator;
    private final RegistrationAccessPolicyResolver policyResolver;
    public UserProfileResult execute(RegisterCommand command) {
        Email email = new Email(command.email());
        if (users.findByEmail(email).isPresent()) {
            throw new IdentityServiceException(
                    new IdentityServiceError.EmailExists(command.email()),
                    "User with this email already exists"
            );
        }

        Platform platform = platforms.findByCode(command.platformCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(command.platformCode()),
                        "Registration platform not found"
                )
        );
        Id userId = idGenerator.generateId();
        HashedPassword password = passwordHasher.hash(command.password());
        User user = User.createLocal(userId, email, password);

        Id assignmentId = idGenerator.generateId();
        RegistrationAccessPolicy policy = policyResolver.resolve(command.platformCode());
        AccessAssignment assignment = policy.createAssignment(assignmentId, userId, platform);

        User saved = users.save(user);
        accessAssignments.save(assignment);

        return new UserProfileResult(
                saved.getId().getValue(),
                saved.getEmail().value(),
                saved.getStatus().name(),
                saved.getCreatedAt().toString()
        );
    }
}

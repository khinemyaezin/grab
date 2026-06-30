package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.RegisterCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.config.IdentityRegistrationProperties;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.RegistrationAccessPolicy;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterCommandHandler implements CommandHandler<RegisterCommand, UserProfileResult> {
    private final UserRepository users;
    private final PlatformRepository platforms;
    private final AccessAssignmentRepository accessAssignments;
    private final PasswordHasher passwordHasher;
    private final IdGenerator idGenerator;
    private final RegistrationAccessPolicy policy;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(RegisterCommand command) {
        Email email = new Email(command.email());
        if (users.findByEmail(email).isPresent()) {
            throw new IdentityServiceException(
                    new IdentityServiceError.EmailExists(command.email()),
                    "User with this email already exists"
            );
        }

        String platformCode = policy.getPlatformCode();
        Platform platform = platforms.findByCode(platformCode).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(platformCode),
                        "Registration platform not found"
                )
        );
        Id userId = idGenerator.generateId();
        HashedPassword password = passwordHasher.hash(command.password());
        User user = User.createLocal(userId, email, password);

        Id assignmentId = idGenerator.generateId();
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

    @Override
    public Class<RegisterCommand> getCommandType() {
        return RegisterCommand.class;
    }
}

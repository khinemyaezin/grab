package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.RegisterCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterCommandHandler implements CommandHandler<RegisterCommand, UserProfileResult> {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final IdGenerator ids;

    @Override
    @IdentityTransactional
    public UserProfileResult handle(RegisterCommand command) {
        if (users.findByEmail(new Email(command.email())).isPresent()) {
            throw new IdentityServiceException(
                    new IdentityServiceError.EmailExists(command.email()),
                    "User with this email already exists"
            );
        }

        User user = User.createLocal(
                ids.generateId(),
                new Email(command.email()),
                passwordHasher.hash(command.password())
        );

        User saved = users.save(user);

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

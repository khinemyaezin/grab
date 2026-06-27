package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.identity.internal.command.AuthResult;
import com.grab.store.identity.internal.command.RegisterUserCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.shared.security.LocalJwtProperties;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.TokenLifeCycle;
import com.identity.domain.service.TokenPair;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RegisterUserCommandHandler implements CommandHandler<RegisterUserCommand, AuthResult> {

    private static final Set<String> SELF_REGISTRATION_ROLES = Set.of("CUSTOMER", "SELLER");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final TokenLifeCycle tokenLifeCycle;
    private final PlatformIdentityResolver identityResolver;
    private final LocalJwtProperties jwtProperties;
    private final IdGenerator idGenerator;

    @Override
    @IdentityTransactional
    public AuthResult handle(RegisterUserCommand command) {
        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new IdentityServiceException(
                    new IdentityServiceError.EmailExists(email.value()),
                    "Email already in use"
            );
        }

        String roleCode = command.role().trim().toUpperCase(Locale.ROOT);
        Role role = roleRepository.findByCode(roleCode)
                .filter(Role::isActive)
                .filter(candidate -> SELF_REGISTRATION_ROLES.contains(candidate.getCode()))
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.InvalidRole(roleCode),
                        "Invalid registration role"
                ));

        User saved = userRepository.save(User.createLocal(
                idGenerator.generateId(),
                email,
                passwordHasher.hash(command.password()),
                role.getCode()
        ));

        if (!saved.isActive()) {
            return new AuthResult(
                    null,
                    null,
                    0,
                    saved.getId().getValue(),
                    email.value(),
                    saved.getRoleCodes(),
                    saved.getStatus().name()
            );
        }

        AuthenticatedActor actor = identityResolver.resolve(new ExternalPrincipal(
                jwtProperties.issuer(),
                saved.getId().getValue(),
                Optional.of(email.value()),
                saved.getRoleCodes()
        ));
        TokenPair tokenPair = tokenLifeCycle.issue(actor);
        return new AuthResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresInMs(),
                actor.platformUserId(),
                actor.email(),
                actor.roles(),
                saved.getStatus().name()
        );
    }

    @Override
    public Class<RegisterUserCommand> getCommandType() {
        return RegisterUserCommand.class;
    }
}

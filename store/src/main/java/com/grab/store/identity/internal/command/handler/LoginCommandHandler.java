package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.identity.internal.command.AuthResult;
import com.grab.store.identity.internal.command.LoginCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.shared.security.LocalJwtProperties;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import com.identity.domain.aggregate.User;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.TokenLifeCycle;
import com.identity.domain.service.TokenPair;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoginCommandHandler implements CommandHandler<LoginCommand, AuthResult> {

    private final UserRepository userRepository;
    private final AccessAssignmentRepository accessAssignments;
    private final PasswordHasher passwordHasher;
    private final TokenLifeCycle tokenLifeCycle;
    private final PlatformIdentityResolver identityResolver;
    private final LocalJwtProperties jwtProperties;

    @Override
    @IdentityTransactional
    public AuthResult handle(LoginCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(this::invalidCredentials);
        if (!user.isActive()
                || user.getPasswordHash().isEmpty()
                || !passwordHasher.verify(command.password(), user.getPasswordHash().orElseThrow())) {
            throw invalidCredentials();
        }

        Optional<AccessContext> accessContext = resolveRequestedContext(command, user);
        AuthenticatedActor actor = identityResolver.resolve(new ExternalPrincipal(
                jwtProperties.issuer(),
                user.getId().getValue(),
                Optional.of(user.getEmail().value()),
                user.getRoleCodes(),
                accessContext
        ));
        TokenPair tokenPair = tokenLifeCycle.issue(actor);
        return new AuthResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresInMs(),
                actor.platformUserId(),
                actor.email(),
                actor.roles(),
                user.getStatus().name()
        );
    }

    @Override
    public Class<LoginCommand> getCommandType() {
        return LoginCommand.class;
    }

    private IdentityAuthenticationException invalidCredentials() {
        return new IdentityAuthenticationException(
                new IdentitySecurityError.InvalidCredentials(),
                "Invalid email or password"
        );
    }

    private Optional<AccessContext> resolveRequestedContext(LoginCommand command, User user) {
        if (command.platformCode() == null) {
            if (command.assignmentId() != null) {
                throw new IdentityServiceException(
                        new IdentityServiceError.AccessContextSelectionInvalid(),
                        "Platform code is required when selecting an assignment"
                );
            }
            return Optional.empty();
        }

        if (command.assignmentId() != null) {
            AccessAssignment assignment = accessAssignments
                    .findById(command.assignmentId())
                    .filter(candidate -> candidate.getUserId().equals(user.getId()))
                    .filter(candidate -> candidate.getPlatformCode().equals(command.platformCode()))
                    .filter(candidate -> candidate.isEffectiveAt(Instant.now()))
                    .orElseThrow(() -> platformAccessUnavailable(command.platformCode()));
            return Optional.of(toContext(assignment));
        }

        List<AccessAssignment> available = accessAssignments.findEffectiveByUserAndPlatform(
                user.getId(), command.platformCode(), Instant.now()
        );
        if (available.isEmpty()) {
            throw platformAccessUnavailable(command.platformCode());
        }
        if (available.size() > 1) {
            throw new IdentityServiceException(
                    new IdentityServiceError.AccessContextSelectionRequired(
                            command.platformCode(), available.size()
                    ),
                    "More than one access context is available; select an assignment"
            );
        }
        return Optional.of(toContext(available.getFirst()));
    }

    private AccessContext toContext(AccessAssignment assignment) {
        return new AccessContext(
                assignment.getPlatformCode(),
                assignment.getId().getValue(),
                assignment.getScope().key().value(),
                assignment.getScope().scopeId()
        );
    }

    private IdentityServiceException platformAccessUnavailable(String platformCode) {
        return new IdentityServiceException(
                new IdentityServiceError.PlatformAccessUnavailable(platformCode),
                "No active access is available for the requested platform"
        );
    }
}

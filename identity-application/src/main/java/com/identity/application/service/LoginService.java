package com.identity.application.service;

import com.identity.application.port.inbound.LoginUseCase;

import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.LoginCommand;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.exception.IdentityServiceException;
import com.identity.application.exception.IdentityServiceError;
import com.identity.domain.aggregate.User;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.service.PasswordHasher;
import com.identity.domain.service.TokenLifeCycle;
import com.identity.domain.service.TokenPair;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final AccessAssignmentRepository accessAssignments;
    private final PasswordHasher passwordHasher;
    private final TokenLifeCycle tokenLifeCycle;
    private final PlatformIdentityResolver identityResolver;
    public AuthResult execute(LoginCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(this::invalidCredentials);
        if (!user.isActive()
                || user.getPasswordHash().isEmpty()
                || !passwordHasher.verify(command.password(), user.getPasswordHash().orElseThrow())) {
            throw invalidCredentials();
        }

        Optional<AccessContext> accessContext = resolveRequestedContext(command, user);
        AuthenticatedActor actor = identityResolver.resolve(new ExternalPrincipal(
                identityResolver.localIssuer(),
                user.getId().getValue(),
                user.getEmail().value(),
                Set.of(),
                accessContext.orElse(null)
        ));
        TokenPair tokenPair = tokenLifeCycle.issue(actor);
        return new AuthResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresInMs(),
                actor.platformUserId(),
                actor.email(),
                actor.roles(),
                user.getStatus().name(),
                accessContext.isEmpty()
        );
    }
private IdentityServiceException invalidCredentials() {
        return new IdentityServiceException(
                new IdentityServiceError.InvalidCredentials(),
                "Invalid email or password"
        );
    }

    private Optional<AccessContext> resolveRequestedContext(LoginCommand command, User user) {
        if (command.platformCode() == null) {
            throw new IdentityServiceException(
                    new IdentityServiceError.PlatformNotFound(""),
                    "Platform code is required when selecting an assignment"
            );
        }

        List<AccessAssignment> available = accessAssignments.findEffectiveByUserAndPlatform(
                user.getId(), command.platformCode(), Instant.now()
        );
        if (available.isEmpty()) {
            throw platformAccessUnavailable(command.platformCode());
        }
        long availableContexts = available.stream()
                .map(assignment -> new ContextKey(
                        assignment.getScope().key().value(),
                        assignment.getScope().scopeId()
                ))
                .distinct()
                .limit(2)
                .count();
        if (availableContexts > 1) {
            return Optional.empty();
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

    private record ContextKey(String scopeKey, String scopeId) {
    }
}

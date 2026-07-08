package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import com.grab.store.identity.internal.command.AuthResult;
import com.grab.store.identity.internal.command.SwitchAccessContextCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.TokenLifeCycle;
import com.identity.domain.service.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SwitchAccessContextCommandHandler implements CommandHandler<SwitchAccessContextCommand, AuthResult> {
    private final UserRepository users;
    private final AccessAssignmentRepository assignments;
    private final PlatformIdentityResolver identityResolver;
    private final TokenLifeCycle tokenLifeCycle;

    @Override
    @IdentityTransactional
    public AuthResult handle(SwitchAccessContextCommand command) {
        User user = users.findById(command.userId()).orElseThrow(() -> new IdentityServiceException(
                new IdentityServiceError.UserNotFound(command.userId().getValue()),
                "User not found"
        ));
        AccessAssignment assignment = assignments.findById(command.assignmentId()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.AccessAssignmentNotFound(command.assignmentId().getValue()),
                        "Access assignment not found"
                )
        );
        if (!assignment.getUserId().getValue().equals(command.userId().getValue())
                || !assignment.isEffectiveAt(Instant.now())) {
            throw new IdentityServiceException(
                    new IdentityServiceError.AccessScopeForbidden(
                            assignment.getScope().key().value(), assignment.getScope().scopeId()
                    ),
                    "Access context is not available to the user"
            );
        }
        AccessContext context = new AccessContext(
                assignment.getPlatformCode(),
                assignment.getId().getValue(),
                assignment.getScope().key().value(),
                assignment.getScope().scopeId()
        );
        AuthenticatedActor actor = identityResolver.resolve(new ExternalPrincipal(
                identityResolver.localIssuer(),
                user.getId().getValue(),
                user.getEmail().value(),
                Set.of(),
                context
        ));
        TokenPair tokenPair = tokenLifeCycle.issue(actor);
        if (command.currentRefreshToken() != null && !command.currentRefreshToken().isBlank()) {
            tokenLifeCycle.revoke(command.currentRefreshToken());
        }
        return new AuthResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresInMs(),
                actor.platformUserId(),
                actor.email(),
                actor.roles(),
                user.getStatus().name(),
                false
        );
    }

    @Override
    public Class<SwitchAccessContextCommand> getCommandType() {
        return SwitchAccessContextCommand.class;
    }
}

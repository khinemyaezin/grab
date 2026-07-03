package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.AcceptAccessInvitationCommand;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.identity.internal.utility.InvitationTokenService;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.AccessInvitationRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleRepository;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AcceptAccessInvitationCommandHandler
        implements CommandHandler<AcceptAccessInvitationCommand, AccessAssignmentResult> {
    private final AccessInvitationRepository invitations;
    private final AccessAssignmentRepository assignments;
    private final PlatformRepository platforms;
    private final RoleRepository roles;
    private final InvitationTokenService tokens;
    private final IdGenerator ids;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(AcceptAccessInvitationCommand command) {
        AccessInvitation invitation = invitations.findByTokenHash(tokens.hash(command.acceptanceToken()))
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.AccessInvitationNotFound(),
                        "Access invitation not found"
                ));
        Instant now = Instant.now();
        invitation.accept(command.userId(), new Email(command.userEmail()), now);
        Platform platform = platforms.findByCode(invitation.getPlatformCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(invitation.getPlatformCode()),
                        "Platform not found"
                )
        );
        Role role = roles.findByCode(invitation.getRoleCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(invitation.getRoleCode()),
                        "Role not found"
                )
        );
        role.requireAssignable();
        if (assignments.existsCurrent(
                command.userId(),
                invitation.getPlatformCode(),
                invitation.getRoleCode(),
                invitation.getScope()
        )) {
            throw new IdentityServiceException(
                    new IdentityServiceError.AccessAssignmentExists(
                            command.userId().getValue(),
                            invitation.getPlatformCode(),
                            invitation.getRoleCode(),
                            invitation.getScope().scopeId()
                    ),
                    "Access assignment already exists"
            );
        }
        invitations.save(invitation);
        AccessAssignment saved = assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.userId(),
                platform,
                invitation.getRoleCode(),
                invitation.getScope(),
                invitation.getInvitedBy(),
                null
        ));
        return AccessAssignmentResult.from(saved);
    }

    @Override
    public Class<AcceptAccessInvitationCommand> getCommandType() {
        return AcceptAccessInvitationCommand.class;
    }
}

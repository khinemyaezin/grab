package com.identity.application.service;

import com.identity.application.port.inbound.AcceptAccessInvitationUseCase;

import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.AcceptAccessInvitationCommand;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.service.InvitationTokenService;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.aggregate.Role;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.AccessInvitationRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import com.identity.domain.port.outbound.SecurityCatalogRepository;

import java.time.Instant;

@RequiredArgsConstructor
public class AcceptAccessInvitationService implements AcceptAccessInvitationUseCase {
    private final SecurityCatalogRepository catalogs;
    private final AccessInvitationRepository invitations;
    private final AccessAssignmentRepository assignments;
    private final RoleRepository roles;
    private final InvitationTokenService invitationTokens;
    private final IdGenerator ids;

    public AccessAssignmentResult execute(AcceptAccessInvitationCommand command) {
        AccessInvitation invitation = invitations.findByTokenHash(invitationTokens.hash(command.acceptanceToken()))
                .orElseThrow(() -> new IdentityServiceException(
                        new IdentityServiceError.AccessInvitationNotFound(),
                        "Access invitation not found"
                ));
        var catalog = catalogs.loadForUpdate();
        catalog.requireEffectiveScope(invitation.getScope().key().value());
        Instant now = Instant.now();
        invitation.accept(command.userId(), new Email(command.userEmail()), now);
        Role role = roles.findByCode(invitation.getRoleCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(invitation.getRoleCode()),
                        "Role not found"
                )
        );
        role.requireAssignable();
        if (assignments.existsCurrent(
                command.userId(),
                invitation.getRoleCode(),
                invitation.getScope()
        )) {
            throw new IdentityServiceException(
                    new IdentityServiceError.AccessAssignmentExists(
                            command.userId().getValue(), invitation.getRoleCode(), invitation.getScope().scopeId()
                    ),
                    "Access assignment already exists"
            );
        }
        invitations.save(invitation);
        AccessAssignment saved = assignments.save(AccessAssignment.create(
                ids.generateId(),
                command.userId(),
                invitation.getRoleCode(),
                invitation.getScope(),
                invitation.getInvitedBy(),
                null
        ));
        return AccessAssignmentResult.from(saved);
    }
}

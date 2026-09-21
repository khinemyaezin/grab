package com.identity.application.service;

import com.identity.application.port.inbound.CancelAccessInvitationUseCase;

import com.identity.application.model.write.AccessInvitationResult;
import com.identity.application.model.write.CancelAccessInvitationCommand;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.port.outbound.AccessInvitationRepository;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CancelAccessInvitationService implements CancelAccessInvitationUseCase {
    private final AccessInvitationRepository invitations;
    private final RoleDelegationPolicy delegationPolicy;
    public AccessInvitationResult execute(CancelAccessInvitationCommand command) {
        AccessInvitation invitation = invitations.findById(command.invitationId()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.AccessInvitationNotFound(),
                        "Access invitation not found"
                )
        );
        AccessScope.from(command.actorScopeKey(), command.actorScopeId())
                .requireEncompasses(invitation.getScope());
        delegationPolicy.requireCanDelegate(command.actorRoleCodes(), invitation.getRoleCode());
        invitation.cancel();
        AccessInvitation saved = invitations.save(invitation);
        return new AccessInvitationResult(
                saved.getId().getValue(),
                saved.getInviteeEmail().value(),
                saved.getPlatformCode(),
                saved.getRoleCode(),
                saved.getScope().key().value(),
                saved.getScope().scopeId(),
                saved.getStatus().name(),
                saved.getExpiresAt().toString(),
                null
        );
    }
}

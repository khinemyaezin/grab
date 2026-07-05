package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.AccessInvitationResult;
import com.grab.store.identity.internal.command.CancelAccessInvitationCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.repository.AccessInvitationRepository;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelAccessInvitationCommandHandler
        implements CommandHandler<CancelAccessInvitationCommand, AccessInvitationResult> {
    private final AccessInvitationRepository invitations;
    private final RoleDelegationPolicy delegationPolicy;

    @Override
    @IdentityTransactional
    public AccessInvitationResult handle(CancelAccessInvitationCommand command) {
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

    @Override
    public Class<CancelAccessInvitationCommand> getCommandType() {
        return CancelAccessInvitationCommand.class;
    }
}

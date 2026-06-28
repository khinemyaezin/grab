package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.command.AccessInvitationResult;
import com.grab.store.identity.internal.command.CreateAccessInvitationCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.exception.IdentityServiceError;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.grab.store.identity.internal.utility.InvitationTokenService;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.User;
import com.identity.domain.repository.AccessInvitationRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.service.AccessRoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateAccessInvitationCommandHandler
        implements CommandHandler<CreateAccessInvitationCommand, AccessInvitationResult> {
    private final PlatformRepository platforms;
    private final UserRepository users;
    private final AccessInvitationRepository invitations;
    private final AccessRoleDelegationPolicy delegationPolicy;
    private final InvitationTokenService tokens;
    private final IdGenerator ids;

    @Override
    @IdentityTransactional
    public AccessInvitationResult handle(CreateAccessInvitationCommand command) {
        Platform platform = platforms.findByCode(command.platformCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(command.platformCode()),
                        "Platform not found"
                )
        );
        User inviter = users.findById(command.invitedBy()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(command.invitedBy().getValue()),
                        "Inviting user not found"
                )
        );
        AccessScope scope = new AccessScope(command.scopeType(), command.scopeId());
        AccessScope.from(command.actorScopeType(), command.actorScopeId()).requireEncompasses(scope);
        delegationPolicy.requireCanDelegate(command.actorRoleCodes(), command.roleCode());
        String acceptanceToken = tokens.generate();
        AccessInvitation saved = invitations.save(AccessInvitation.create(
                ids.generateId(),
                new Email(command.inviteeEmail()),
                platform,
                command.roleCode(),
                scope,
                tokens.hash(acceptanceToken),
                command.invitedBy(),
                inviter.getEmail(),
                command.expiresAt()
        ));
        return new AccessInvitationResult(
                saved.getId().getValue(),
                saved.getInviteeEmail().value(),
                saved.getPlatformCode(),
                saved.getRoleCode(),
                saved.getScope().type().name(),
                saved.getScope().scopeId(),
                saved.getStatus().name(),
                saved.getExpiresAt().toString(),
                acceptanceToken
        );
    }

    @Override
    public Class<CreateAccessInvitationCommand> getCommandType() {
        return CreateAccessInvitationCommand.class;
    }
}

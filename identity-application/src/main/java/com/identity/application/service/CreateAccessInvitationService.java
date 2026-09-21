package com.identity.application.service;

import com.identity.application.port.inbound.CreateAccessInvitationUseCase;

import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.AccessInvitationResult;
import com.identity.application.model.write.CreateAccessInvitationCommand;
import com.identity.application.exception.IdentityServiceError;
import com.identity.application.exception.IdentityServiceException;
import com.identity.domain.service.InvitationTokenService;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.User;
import com.identity.domain.port.outbound.AccessInvitationRepository;
import com.identity.domain.port.outbound.PlatformRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.policy.RoleDelegationPolicy;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateAccessInvitationService implements CreateAccessInvitationUseCase {
    private final PlatformRepository platforms;
    private final RoleRepository roles;
    private final UserRepository users;
    private final AccessInvitationRepository invitations;
    private final RoleDelegationPolicy delegationPolicy;
    private final InvitationTokenService invitationTokens;
    private final IdGenerator ids;
    public AccessInvitationResult execute(CreateAccessInvitationCommand command) {
        Platform platform = platforms.findByCode(command.platformCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.PlatformNotFound(command.platformCode()),
                        "Platform not found"
                )
        );
        Role role = roles.findByCode(command.roleCode()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.RoleNotFound(command.roleCode()),
                        "Role not found"
                )
        );
        role.requireAssignable();
        User inviter = users.findById(command.invitedBy()).orElseThrow(() ->
                new IdentityServiceException(
                        new IdentityServiceError.UserNotFound(command.invitedBy().getValue()),
                        "Inviting user not found"
                )
        );
        AccessScope scope = AccessScope.from(command.scopeKey(), command.scopeId());
        AccessScope.from(command.actorScopeKey(), command.actorScopeId()).requireEncompasses(scope);
        delegationPolicy.requireCanDelegate(command.actorRoleCodes(), command.roleCode());
        String acceptanceToken = invitationTokens.generate();
        AccessInvitation saved = invitations.save(AccessInvitation.create(
                ids.generateId(),
                new Email(command.inviteeEmail()),
                platform,
                command.roleCode(),
                scope,
                invitationTokens.hash(acceptanceToken),
                command.invitedBy(),
                inviter.getEmail(),
                command.expiresAt()
        ));
        return new AccessInvitationResult(
                saved.getId().getValue(),
                saved.getInviteeEmail().value(),
                saved.getPlatformCode(),
                saved.getRoleCode(),
                saved.getScope().key().value(),
                saved.getScope().scopeId(),
                saved.getStatus().name(),
                saved.getExpiresAt().toString(),
                acceptanceToken
        );
    }
}

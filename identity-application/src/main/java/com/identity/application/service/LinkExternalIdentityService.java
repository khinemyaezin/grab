package com.identity.application.service;

import com.identity.application.exception.ExternalIdentityLinkConflictException;
import com.identity.application.model.write.ExternalIdentityLinkResult;
import com.identity.application.model.write.LinkExternalIdentityCommand;
import com.identity.application.port.inbound.LinkExternalIdentityUseCase;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class LinkExternalIdentityService implements LinkExternalIdentityUseCase {
    private final UserRepository users;
    private final ExternalIdentityRepository externalIdentities;
    private final Clock clock;

    @Override
    public ExternalIdentityLinkResult execute(LinkExternalIdentityCommand command) {
        User user = users.findByIdForUpdate(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("Platform user not found"));
        ExternalIdentity current = externalIdentities.findByIssuerAndSubject(command.issuer(), command.subject())
                .orElse(null);
        if (current != null) {
            if (!current.userId().equals(command.userId())
                    || !java.util.Objects.equals(current.directoryId(), command.directoryId())
                    || !java.util.Objects.equals(current.resourceId(), command.resourceId())) {
                throw new ExternalIdentityLinkConflictException();
            }
            user.markExternallyManaged();
            users.save(user);
            return new ExternalIdentityLinkResult(command.userId(), command.issuer(), command.subject(), true);
        }
        if (command.directoryId() != null && command.resourceId() != null) {
            ExternalIdentity byResource = externalIdentities
                    .findByDirectoryIdAndResourceId(command.directoryId(), command.resourceId()).orElse(null);
            if (byResource != null) {
                throw new ExternalIdentityLinkConflictException();
            }
        }
        ExternalIdentity identity = ExternalIdentity.link(command.userId(), command.issuer(), command.subject(),
                command.directoryId(), command.resourceId(), command.actorId(), LocalDateTime.now(clock));
        externalIdentities.save(identity);
        user.markExternallyManaged();
        users.save(user);
        return new ExternalIdentityLinkResult(command.userId(), command.issuer(), command.subject(), false);
    }
}

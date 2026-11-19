package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.UpdateExternalUserStatusCommand;
import com.identity.application.port.inbound.UpdateExternalUserUseCase;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UpdateExternalUserService implements UpdateExternalUserUseCase {
    private static final String DEFAULT_ISSUER = "authorization-server";

    private final UserRepository users;
    private final ExternalIdentityRepository externalIdentities;
    private final IdGenerator idGenerator;

    @Override
    public ExternalUserProvisionResult execute(UpdateExternalUserStatusCommand command) {
        if (command.idOrExternalId() == null || command.idOrExternalId().isBlank()) {
            throw new IllegalArgumentException("User identifier is required");
        }
        String identifier = command.idOrExternalId().trim();
        User user = null;
        String extId = null;

        // 1. Try finding by externalId / subject
        Optional<ExternalIdentity> linked = externalIdentities.findByIssuerAndSubject(DEFAULT_ISSUER, identifier);
        if (linked.isPresent()) {
            extId = identifier;
            user = users.findById(linked.get().userId()).orElse(null);
        }

        // 2. Try finding by platform userId
        if (user == null) {
            try {
                Id userId = idGenerator.convertIdFrom(identifier);
                user = users.findById(userId).orElse(null);
                if (user != null) {
                    extId = externalIdentities.findByUserId(userId).map(ExternalIdentity::subject).orElse(null);
                }
            } catch (Exception ignored) {
            }
        }

        // 3. Try finding by email
        if (user == null) {
            try {
                Email email = new Email(identifier);
                user = users.findByEmail(email).orElse(null);
                if (user != null) {
                    extId = externalIdentities.findByUserId(user.getId()).map(ExternalIdentity::subject).orElse(null);
                }
            } catch (Exception ignored) {
            }
        }

        if (user == null) {
            throw new IllegalArgumentException("User not found for identifier: " + identifier);
        }

        if (command.active()) {
            if (user.getStatus() == UserStatus.SUSPENDED) {
                user.reactivate();
                users.save(user);
            }
        } else {
            if (user.getStatus() != UserStatus.SUSPENDED) {
                user.suspend();
                users.save(user);
            }
        }

        return new ExternalUserProvisionResult(
                user.getId(),
                extId,
                user.getEmail().value(),
                null,
                null,
                user.getStatus().name(),
                false
        );
    }
}

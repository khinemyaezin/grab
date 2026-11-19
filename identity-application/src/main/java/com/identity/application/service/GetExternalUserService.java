package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.model.read.ExternalUserView;
import com.identity.application.model.read.GetExternalUserQuery;
import com.identity.application.port.inbound.GetExternalUserUseCase;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.aggregate.User;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class GetExternalUserService implements GetExternalUserUseCase {
    private static final String DEFAULT_ISSUER = "authorization-server";

    private final UserRepository users;
    private final ExternalIdentityRepository externalIdentities;
    private final IdGenerator idGenerator;

    @Override
    public ExternalUserView execute(GetExternalUserQuery query) {
        if (query.idOrExternalId() == null || query.idOrExternalId().isBlank()) {
            return null;
        }
        String identifier = query.idOrExternalId().trim();

        // 1. Try finding by externalId / subject
        Optional<ExternalIdentity> linked = externalIdentities.findByIssuerAndSubject(DEFAULT_ISSUER, identifier);
        if (linked.isPresent()) {
            Optional<User> userOpt = users.findById(linked.get().userId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return new ExternalUserView(
                        user.getId(),
                        identifier,
                        user.getEmail().value(),
                        user.getStatus().name(),
                        user.getAuthenticationVersion()
                );
            }
        }

        // 2. Try finding by platform userId (Id)
        try {
            Id userId = idGenerator.convertIdFrom(identifier);
            Optional<User> userOpt = users.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String extId = externalIdentities.findByUserId(userId)
                        .map(ExternalIdentity::subject)
                        .orElse(null);
                return new ExternalUserView(
                        user.getId(),
                        extId,
                        user.getEmail().value(),
                        user.getStatus().name(),
                        user.getAuthenticationVersion()
                );
            }
        } catch (Exception ignored) {
            // Not a valid Id format, ignore
        }

        // 3. Try finding by email
        try {
            Email email = new Email(identifier);
            Optional<User> userOpt = users.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String extId = externalIdentities.findByUserId(user.getId())
                        .map(ExternalIdentity::subject)
                        .orElse(null);
                return new ExternalUserView(
                        user.getId(),
                        extId,
                        user.getEmail().value(),
                        user.getStatus().name(),
                        user.getAuthenticationVersion()
                );
            }
        } catch (Exception ignored) {
            // Not a valid email format, ignore
        }

        return null;
    }
}

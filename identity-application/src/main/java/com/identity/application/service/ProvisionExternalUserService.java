package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.ProvisionExternalUserCommand;
import com.identity.application.port.inbound.ProvisionExternalUserUseCase;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.aggregate.User;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class ProvisionExternalUserService implements ProvisionExternalUserUseCase {
    private static final String DEFAULT_ISSUER = "authorization-server";

    private final UserRepository users;
    private final ExternalIdentityRepository externalIdentities;
    private final IdGenerator idGenerator;

    @Override
    public ExternalUserProvisionResult execute(ProvisionExternalUserCommand command) {
        Objects.requireNonNull(command, "command is required");
        String emailStr = command.email();
        if (emailStr == null || emailStr.isBlank()) {
            throw new IllegalArgumentException("Email is required for provisioning");
        }
        Email email = new Email(emailStr.trim().toLowerCase());
        String externalId = command.externalId() != null && !command.externalId().isBlank() ? command.externalId().trim() : null;

        if (externalId != null) {
            Optional<ExternalIdentity> linked = externalIdentities.findByIssuerAndSubject(DEFAULT_ISSUER, externalId);
            if (linked.isPresent()) {
                Id existingUserId = linked.get().userId();
                User user = users.findById(existingUserId).orElseThrow(() ->
                        new IllegalStateException("Linked user not found for ID: " + existingUserId.getValue()));
                return new ExternalUserProvisionResult(
                        user.getId(),
                        externalId,
                        user.getEmail().value(),
                        command.givenName(),
                        command.familyName(),
                        user.getStatus().name(),
                        false
                );
            }
        }

        Optional<User> existingUser = users.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (externalId != null) {
                Optional<ExternalIdentity> userLink = externalIdentities.findByUserId(user.getId());
                if (userLink.isEmpty()) {
                    ExternalIdentity identity = ExternalIdentity.link(
                            user.getId(),
                            DEFAULT_ISSUER,
                            externalId,
                            null,
                            null,
                            "scim-provisioner",
                            LocalDateTime.now()
                    );
                    externalIdentities.save(identity);
                }
                user.markExternallyManaged();
                users.save(user);
            }
            return new ExternalUserProvisionResult(
                    user.getId(),
                    externalId,
                    user.getEmail().value(),
                    command.givenName(),
                    command.familyName(),
                    user.getStatus().name(),
                    false
            );
        }

        Id newUserId = idGenerator.generateId();
        User newUser = User.createExternal(newUserId, email);
        User savedUser = users.save(newUser);

        if (externalId != null) {
            ExternalIdentity identity = ExternalIdentity.link(
                    newUserId,
                    DEFAULT_ISSUER,
                    externalId,
                    null,
                    null,
                    "scim-provisioner",
                    LocalDateTime.now()
            );
            externalIdentities.save(identity);
        }

        return new ExternalUserProvisionResult(
                savedUser.getId(),
                externalId,
                savedUser.getEmail().value(),
                command.givenName(),
                command.familyName(),
                savedUser.getStatus().name(),
                true
        );
    }
}

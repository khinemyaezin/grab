package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.identity.application.exception.ExternalIdentityLinkConflictException;
import com.identity.application.model.write.ExternalIdentityLinkResult;
import com.identity.application.model.write.LinkExternalIdentityCommand;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkExternalIdentityServiceTest {
    @Test
    void linkExternalIdentityIsIdempotentAndDisablesLocalCredentials() {
        FakeUsers users = new FakeUsers();
        User user = User.createLocal(new CommonId("platform-user"), new Email("seller@example.com"),
                new HashedPassword("local-hash"));
        user.restoreAuthenticationVersion(4);
        users.save(user);
        FakeExternalIdentities identities = new FakeExternalIdentities();
        LinkExternalIdentityService service = new LinkExternalIdentityService(users, identities,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
        LinkExternalIdentityCommand command = new LinkExternalIdentityCommand(user.getId(),
                "https://issuer.example", "external-subject", "directory-1", "resource-1", "admin-1");

        ExternalIdentityLinkResult created = service.execute(command);
        ExternalIdentityLinkResult repeated = service.execute(command);

        assertFalse(created.alreadyLinked());
        assertTrue(repeated.alreadyLinked());
        assertFalse(user.isLocallyManaged());
        assertTrue(user.getPasswordHash().isEmpty());
        assertEquals(5, user.getAuthenticationVersion());
        assertEquals("admin-1", identities.saved.updatedBy());
    }

    @Test
    void linkExternalIdentityRejectsSubjectAlreadyLinkedToAnotherUser() {
        FakeUsers users = new FakeUsers();
        User owner = User.createLocal(new CommonId("owner"), new Email("owner@example.com"),
                new HashedPassword("local-hash"));
        User other = User.createLocal(new CommonId("other"), new Email("other@example.com"),
                new HashedPassword("local-hash"));
        users.save(owner);
        users.save(other);
        FakeExternalIdentities identities = new FakeExternalIdentities();
        LinkExternalIdentityService service = new LinkExternalIdentityService(users, identities, Clock.systemUTC());
        service.execute(command(owner.getId(), "same-subject", "resource-owner"));

        assertThrows(ExternalIdentityLinkConflictException.class,
                () -> service.execute(command(other.getId(), "same-subject", "resource-other")));
    }

    private LinkExternalIdentityCommand command(Id userId, String subject, String resourceId) {
        return new LinkExternalIdentityCommand(userId, "https://issuer.example", subject,
                "directory-1", resourceId, "admin-1");
    }

    private static class FakeUsers implements UserRepository {
        private final Map<String, User> values = new HashMap<>();

        @Override
        public Optional<User> findById(Id id) {
            return Optional.ofNullable(values.get(id.getValue()));
        }

        @Override
        public Optional<User> findByIdForUpdate(Id id) {
            return findById(id);
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return values.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
        }

        @Override
        public boolean existsByEmail(Email email) {
            return findByEmail(email).isPresent();
        }

        @Override
        public User save(User user) {
            values.put(user.getId().getValue(), user);
            return user;
        }
    }

    private static class FakeExternalIdentities implements ExternalIdentityRepository {
        private final Map<String, ExternalIdentity> values = new HashMap<>();
        private ExternalIdentity saved;

        @Override
        public Optional<ExternalIdentity> findByIssuerAndSubject(String issuer, String subject) {
            return Optional.ofNullable(values.get(issuer + "|" + subject));
        }

        @Override
        public Optional<ExternalIdentity> findByDirectoryIdAndResourceId(String directoryId, String resourceId) {
            return values.values().stream().filter(identity -> directoryId.equals(identity.directoryId())
                    && resourceId.equals(identity.resourceId())).findFirst();
        }

        @Override
        public Optional<ExternalIdentity> findByUserId(Id userId) {
            return values.values().stream().filter(identity -> userId.equals(identity.userId())).findFirst();
        }

        @Override
        public ExternalIdentity save(ExternalIdentity identity) {
            saved = identity;
            values.put(identity.issuer() + "|" + identity.subject(), identity);
            return identity;
        }
    }
}

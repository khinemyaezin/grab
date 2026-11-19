package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.ProvisionExternalUserCommand;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.IdentityManagementMode;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProvisionExternalUserServiceTest {
    private FakeUserRepository users;
    private FakeExternalIdentityRepository externalIdentities;
    private IdGenerator idGenerator;
    private ProvisionExternalUserService service;

    @BeforeEach
    void setUp() {
        users = new FakeUserRepository();
        externalIdentities = new FakeExternalIdentityRepository();
        AtomicInteger counter = new AtomicInteger(1);
        idGenerator = new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("user-" + counter.getAndIncrement());
            }

            @Override
            public Id convertIdFrom(String rawId) {
                return new CommonId(rawId);
            }
        };
        service = new ProvisionExternalUserService(users, externalIdentities, idGenerator);
    }

    @Test
    void execute_newUserWithExternalId_provisionsUserAndLinksExternalIdentity() {
        ProvisionExternalUserCommand command = new ProvisionExternalUserCommand(
                "iam-sub-123",
                "new-seller@example.com",
                "John",
                "Doe"
        );

        ExternalUserProvisionResult result = service.execute(command);

        assertThat(result.newlyCreated()).isTrue();
        assertThat(result.externalId()).isEqualTo("iam-sub-123");
        assertThat(result.email()).isEqualTo("new-seller@example.com");
        assertThat(result.status()).isEqualTo("ACTIVE");

        User savedUser = users.findById(result.userId()).orElseThrow();
        assertThat(savedUser.getIdentityManagementMode()).isEqualTo(IdentityManagementMode.EXTERNAL);
        assertThat(savedUser.getPasswordHash()).isEmpty();

        ExternalIdentity linked = externalIdentities.findByIssuerAndSubject("authorization-server", "iam-sub-123").orElseThrow();
        assertThat(linked.userId()).isEqualTo(result.userId());
    }

    @Test
    void execute_existingUserByExternalId_returnsExistingUserWithoutRecreating() {
        // Pre-create user and link
        Id existingId = new CommonId("existing-user-1");
        User existingUser = User.createExternal(existingId, new Email("existing@example.com"));
        users.save(existingUser);
        ExternalIdentity identity = ExternalIdentity.link(
                existingId, "authorization-server", "iam-sub-existing", null, null, "test", java.time.LocalDateTime.now()
        );
        externalIdentities.save(identity);

        ProvisionExternalUserCommand command = new ProvisionExternalUserCommand(
                "iam-sub-existing",
                "existing@example.com",
                "Existing",
                "User"
        );

        ExternalUserProvisionResult result = service.execute(command);

        assertThat(result.newlyCreated()).isFalse();
        assertThat(result.userId()).isEqualTo(existingId);
        assertThat(result.email()).isEqualTo("existing@example.com");
    }

    @Test
    void execute_existingUserByEmailWithoutLink_linksExternalIdAndReturnsExistingUser() {
        Id existingId = new CommonId("existing-user-2");
        User existingUser = User.createExternal(existingId, new Email("seller@example.com"));
        users.save(existingUser);

        ProvisionExternalUserCommand command = new ProvisionExternalUserCommand(
                "iam-sub-newlink",
                "seller@example.com",
                "Seller",
                "One"
        );

        ExternalUserProvisionResult result = service.execute(command);

        assertThat(result.newlyCreated()).isFalse();
        assertThat(result.userId()).isEqualTo(existingId);

        ExternalIdentity linked = externalIdentities.findByIssuerAndSubject("authorization-server", "iam-sub-newlink").orElseThrow();
        assertThat(linked.userId()).isEqualTo(existingId);
    }

    @Test
    void execute_missingEmail_throwsException() {
        ProvisionExternalUserCommand command = new ProvisionExternalUserCommand("iam-1", "", "First", "Last");
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static class FakeUserRepository implements UserRepository {
        private final Map<String, User> byId = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();

        @Override
        public Optional<User> findById(Id id) {
            return Optional.ofNullable(byId.get(id.getValue()));
        }

        @Override
        public Optional<User> findByIdForUpdate(Id id) {
            return findById(id);
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return Optional.ofNullable(byEmail.get(email.value()));
        }

        @Override
        public boolean existsByEmail(Email email) {
            return byEmail.containsKey(email.value());
        }

        @Override
        public User save(User user) {
            byId.put(user.getId().getValue(), user);
            byEmail.put(user.getEmail().value(), user);
            return user;
        }
    }

    private static class FakeExternalIdentityRepository implements ExternalIdentityRepository {
        private final Map<String, ExternalIdentity> byKey = new HashMap<>();
        private final Map<Id, ExternalIdentity> byUserId = new HashMap<>();

        @Override
        public Optional<ExternalIdentity> findByIssuerAndSubject(String issuer, String subject) {
            return Optional.ofNullable(byKey.get(issuer + ":" + subject));
        }

        @Override
        public Optional<ExternalIdentity> findByDirectoryIdAndResourceId(String directoryId, String resourceId) {
            return Optional.empty();
        }

        @Override
        public Optional<ExternalIdentity> findByUserId(Id userId) {
            return Optional.ofNullable(byUserId.get(userId));
        }

        @Override
        public ExternalIdentity save(ExternalIdentity identity) {
            byKey.put(identity.issuer() + ":" + identity.subject(), identity);
            byUserId.put(identity.userId(), identity);
            return identity;
        }
    }
}

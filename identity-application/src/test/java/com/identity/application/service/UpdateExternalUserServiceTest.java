package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.UpdateExternalUserStatusCommand;
import com.identity.domain.aggregate.ExternalIdentity;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.port.outbound.ExternalIdentityRepository;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateExternalUserServiceTest {
    private FakeUserRepository users;
    private FakeExternalIdentityRepository externalIdentities;
    private UpdateExternalUserService service;

    @BeforeEach
    void setUp() {
        users = new FakeUserRepository();
        externalIdentities = new FakeExternalIdentityRepository();
        IdGenerator idGenerator = new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("generated");
            }

            @Override
            public Id convertIdFrom(String rawId) {
                return new CommonId(rawId);
            }
        };
        service = new UpdateExternalUserService(users, externalIdentities, idGenerator);

        Id userId = new CommonId("user-uuid-1");
        User user = User.createExternal(userId, new Email("user@example.com"));
        users.save(user);

        ExternalIdentity identity = ExternalIdentity.link(
                userId, "authorization-server", "iam-subject-1", null, null, "test", LocalDateTime.now()
        );
        externalIdentities.save(identity);
    }

    @Test
    void execute_suspendUser_updatesStatusToSuspended() {
        ExternalUserProvisionResult result = service.execute(new UpdateExternalUserStatusCommand("iam-subject-1", false));

        assertThat(result.status()).isEqualTo("SUSPENDED");
        User user = users.findById(new CommonId("user-uuid-1")).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void execute_reactivateUser_updatesStatusToActive() {
        service.execute(new UpdateExternalUserStatusCommand("iam-subject-1", false));
        ExternalUserProvisionResult result = service.execute(new UpdateExternalUserStatusCommand("iam-subject-1", true));

        assertThat(result.status()).isEqualTo("ACTIVE");
        User user = users.findById(new CommonId("user-uuid-1")).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void execute_unknownUser_throwsException() {
        assertThatThrownBy(() -> service.execute(new UpdateExternalUserStatusCommand("unknown", false)))
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

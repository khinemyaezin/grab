package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.identity.application.model.read.ExternalUserView;
import com.identity.application.model.read.GetExternalUserQuery;
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

class GetExternalUserServiceTest {
    private FakeUserRepository users;
    private FakeExternalIdentityRepository externalIdentities;
    private IdGenerator idGenerator;
    private GetExternalUserService service;

    @BeforeEach
    void setUp() {
        users = new FakeUserRepository();
        externalIdentities = new FakeExternalIdentityRepository();
        idGenerator = new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("generated");
            }

            @Override
            public Id convertIdFrom(String rawId) {
                return new CommonId(rawId);
            }
        };
        service = new GetExternalUserService(users, externalIdentities, idGenerator);

        Id userId = new CommonId("user-uuid-123");
        User user = User.createExternal(userId, new Email("seller@example.com"));
        users.save(user);

        ExternalIdentity identity = ExternalIdentity.link(
                userId, "authorization-server", "iam-subject-789", null, null, "test", LocalDateTime.now()
        );
        externalIdentities.save(identity);
    }

    @Test
    void execute_lookupByExternalSubject_returnsUser() {
        ExternalUserView view = service.execute(new GetExternalUserQuery("iam-subject-789"));

        assertThat(view).isNotNull();
        assertThat(view.userId().getValue()).isEqualTo("user-uuid-123");
        assertThat(view.externalId()).isEqualTo("iam-subject-789");
        assertThat(view.email()).isEqualTo("seller@example.com");
        assertThat(view.status()).isEqualTo("ACTIVE");
    }

    @Test
    void execute_lookupByPlatformUserId_returnsUser() {
        ExternalUserView view = service.execute(new GetExternalUserQuery("user-uuid-123"));

        assertThat(view).isNotNull();
        assertThat(view.userId().getValue()).isEqualTo("user-uuid-123");
        assertThat(view.externalId()).isEqualTo("iam-subject-789");
        assertThat(view.email()).isEqualTo("seller@example.com");
    }

    @Test
    void execute_lookupByEmail_returnsUser() {
        ExternalUserView view = service.execute(new GetExternalUserQuery("seller@example.com"));

        assertThat(view).isNotNull();
        assertThat(view.userId().getValue()).isEqualTo("user-uuid-123");
        assertThat(view.externalId()).isEqualTo("iam-subject-789");
    }

    @Test
    void execute_nonExistent_returnsNull() {
        ExternalUserView view = service.execute(new GetExternalUserQuery("unknown-id"));
        assertThat(view).isNull();
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

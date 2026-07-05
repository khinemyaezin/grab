package com.grab.store.identity.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AccessContext;
import com.grab.store.identity.internal.command.ReplaceAccessCommand;
import com.grab.store.identity.internal.exception.IdentityServiceException;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.aggregate.User;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.policy.AccessPlacementPolicy;
import com.identity.domain.policy.AccessPlacementPolicyResolver;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.SessionStore;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.SessionDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplaceAccessCommandHandlerTest {
    private static final String PREVIOUS_ROLE = "REVIEWER";
    private static final String REPLACEMENT_ROLE = "APPROVER";

    private final StubUserRepository users = new StubUserRepository();
    private final StubPlatformRepository platforms = new StubPlatformRepository();
    private final RecordingAssignments assignments = new RecordingAssignments();
    private final RecordingSessionStore sessions = new RecordingSessionStore();

    private ReplaceAccessCommandHandler handler;

    @BeforeEach
    void setUp() {
        users.user = user();
        platforms.platform = platform();
        platforms.findCalled = false;
        assignments.current.clear();
        assignments.saved.clear();
        sessions.revokedAssignmentIds.clear();
        handler = handlerWithPolicies(List.of(placementPolicy()));
    }

    @Test
    void handle_withValidCommand_shouldCreateReplacementAccess() {
        var result = handler.handle(command());

        assertThat(result.id()).isEqualTo("replacement-assignment");
        assertThat(result.roleCode()).isEqualTo(REPLACEMENT_ROLE);
        assertThat(result.scopeId()).isEqualTo("organization-1");
        assertThat(assignments.saved).hasSize(1);
    }

    @Test
    void handle_withPreviousAccess_shouldRevokeAccessAndItsSessions() {
        AccessAssignment previous = assignment(
                "previous-assignment",
                PREVIOUS_ROLE,
                AccessScope.global()
        );
        assignments.current.add(previous);

        handler.handle(command());

        assertThat(previous.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        assertThat(assignments.saved).contains(previous);
        assertThat(sessions.revokedAssignmentIds)
                .containsExactly("previous-assignment");
    }

    @Test
    void handle_withExistingReplacement_shouldReturnItWithoutCreatingAnother() {
        AccessAssignment existing = assignment(
                "existing-replacement",
                REPLACEMENT_ROLE,
                targetScope()
        );
        assignments.current.add(existing);

        var result = handler.handle(command());

        assertThat(result.id()).isEqualTo("existing-replacement");
        assertThat(assignments.saved).isEmpty();
    }

    @Test
    void handle_withSuspendedReplacement_shouldRevokeItAndCreateActiveReplacement() {
        AccessAssignment suspended = assignment(
                "suspended-replacement",
                REPLACEMENT_ROLE,
                targetScope(),
                AccessAssignmentStatus.SUSPENDED,
                null
        );
        assignments.current.add(suspended);

        var result = handler.handle(command());

        assertThat(suspended.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        assertThat(result.id()).isEqualTo("replacement-assignment");
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
        assertThat(sessions.revokedAssignmentIds)
                .containsExactly("suspended-replacement");
    }

    @Test
    void handle_withExpiredReplacement_shouldExpireItAndCreateActiveReplacement() {
        AccessAssignment expired = assignment(
                "expired-replacement",
                REPLACEMENT_ROLE,
                targetScope(),
                AccessAssignmentStatus.ACTIVE,
                Instant.parse("2020-01-01T00:00:00Z")
        );
        assignments.current.add(expired);

        var result = handler.handle(command());

        assertThat(expired.getStatus()).isEqualTo(AccessAssignmentStatus.EXPIRED);
        assertThat(result.id()).isEqualTo("replacement-assignment");
        assertThat(result.status()).isEqualTo(AccessAssignmentStatus.ACTIVE.name());
        assertThat(sessions.revokedAssignmentIds)
                .containsExactly("expired-replacement");
    }

    @Test
    void handle_withUnknownUser_shouldFailBeforeLookingUpPlatform() {
        users.user = null;

        assertThatThrownBy(() -> handler.handle(command()))
                .isInstanceOf(IdentityServiceException.class);

        assertThat(platforms.findCalled).isFalse();
    }

    @Test
    void handle_withUnknownPlatform_shouldFailBeforeChangingAccess() {
        platforms.platform = null;

        assertThatThrownBy(() -> handler.handle(command()))
                .isInstanceOf(IdentityServiceException.class);

        assertThat(assignments.saved).isEmpty();
    }

    @Test
    void handle_withoutPlacementPolicy_shouldRejectReplacementRole() {
        handler = handlerWithPolicies(List.of());

        assertThatThrownBy(() -> handler.handle(command()))
                .isInstanceOf(IdentityServiceException.class)
                .hasMessage("Access placement policy not found");

        assertThat(assignments.saved).isEmpty();
    }

    private ReplaceAccessCommandHandler handlerWithPolicies(
            List<AccessPlacementPolicy> policies
    ) {
        return new ReplaceAccessCommandHandler(
                users,
                platforms,
                assignments,
                sessions,
                fixedIds(),
                new AccessPlacementPolicyResolver(policies)
        );
    }

    private AccessPlacementPolicy placementPolicy() {
        return new AccessPlacementPolicy() {
            @Override
            public String placementRoleCode() {
                return REPLACEMENT_ROLE;
            }

            @Override
            public AccessPlacementPlan plan(
                    Platform platform,
                    AccessScope targetScope
            ) {
                return new AccessPlacementPlan(
                        platform.requireSupportedRole(PREVIOUS_ROLE),
                        AccessScope.global(),
                        platform.requireSupportedRole(REPLACEMENT_ROLE),
                        targetScope
                );
            }
        };
    }

    private ReplaceAccessCommand command() {
        return new ReplaceAccessCommand(
                new CommonId("user-1"),
                "PORTAL",
                REPLACEMENT_ROLE,
                "organization.account",
                "organization-1"
        );
    }

    private AccessScope targetScope() {
        return AccessScope.from("organization.account", "organization-1");
    }

    private AccessAssignment assignment(
            String id,
            String roleCode,
            AccessScope scope
    ) {
        return assignment(id, roleCode, scope, AccessAssignmentStatus.ACTIVE, null);
    }

    private AccessAssignment assignment(
            String id,
            String roleCode,
            AccessScope scope,
            AccessAssignmentStatus status,
            Instant expiresAt
    ) {
        Instant now = Instant.parse("2026-07-02T00:00:00Z");
        return new AccessAssignment(
                new CommonId(id),
                new CommonId("user-1"),
                "PORTAL",
                roleCode,
                scope,
                status,
                null,
                now,
                now,
                expiresAt
        );
    }

    private User user() {
        LocalDateTime now = LocalDateTime.parse("2026-07-02T00:00:00");
        return new User(
                new CommonId("user-1"),
                new Email("user@example.com"),
                null,
                UserStatus.ACTIVE,
                now,
                now
        );
    }

    private Platform platform() {
        return new Platform(
                new CommonId("platform-1"),
                "PORTAL",
                "Portal",
                true,
                Set.of(PREVIOUS_ROLE, REPLACEMENT_ROLE)
        );
    }

    private IdGenerator fixedIds() {
        return new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("replacement-assignment");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
    }

    private static final class StubUserRepository implements UserRepository {
        private User user;

        @Override
        public Optional<User> findById(Id id) {
            return Optional.ofNullable(user);
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return Optional.empty();
        }

        @Override
        public boolean existsByEmail(Email email) {
            return false;
        }

        @Override
        public User save(User user) {
            return user;
        }
    }

    private static final class StubPlatformRepository implements PlatformRepository {
        private Platform platform;
        private boolean findCalled;

        @Override
        public Optional<Platform> findByCode(String code) {
            findCalled = true;
            return Optional.ofNullable(platform);
        }

        @Override
        public Set<Platform> findByRoleCode(String roleCode) {
            return Set.of();
        }

        @Override
        public Platform save(Platform platform) {
            return platform;
        }
    }

    private static final class RecordingAssignments
            implements AccessAssignmentRepository {
        private final List<AccessAssignment> current = new ArrayList<>();
        private final List<AccessAssignment> saved = new ArrayList<>();

        @Override
        public Optional<AccessAssignment> findById(Id id) {
            return Optional.empty();
        }

        @Override
        public Optional<AccessAssignment> findCurrent(
                Id userId,
                String platformCode,
                String roleCode,
                AccessScope scope
        ) {
            return current.stream()
                    .filter(assignment -> assignment.getUserId().equals(userId))
                    .filter(assignment -> assignment.getPlatformCode().equals(platformCode))
                    .filter(assignment -> assignment.getRoleCode().equals(roleCode))
                    .filter(assignment -> assignment.getScope().equals(scope))
                    .filter(assignment -> assignment.getStatus() == AccessAssignmentStatus.ACTIVE
                            || assignment.getStatus() == AccessAssignmentStatus.SUSPENDED)
                    .findFirst();
        }

        @Override
        public List<AccessAssignment> findEffectiveByUserAndPlatform(
                Id userId,
                String platformCode,
                Instant now
        ) {
            return List.of();
        }

        @Override
        public List<AccessAssignment> findByUser(Id userId) {
            return List.copyOf(current);
        }

        @Override
        public boolean existsEffective(
                Id userId,
                String platformCode,
                String roleCode,
                AccessScope scope,
                Instant now
        ) {
            return false;
        }

        @Override
        public boolean existsCurrent(
                Id userId,
                String platformCode,
                String roleCode,
                AccessScope scope
        ) {
            return findCurrent(userId, platformCode, roleCode, scope).isPresent();
        }

        @Override
        public AccessAssignment save(AccessAssignment assignment) {
            saved.add(assignment);
            if (current.stream().noneMatch(value -> value.getId().equals(assignment.getId()))) {
                current.add(assignment);
            }
            return assignment;
        }
    }

    private static final class RecordingSessionStore implements SessionStore {
        private final List<String> revokedAssignmentIds = new ArrayList<>();

        @Override
        public void saveNewSession(
                String userId,
                String tokenHash,
                String tokenFamilyId,
                Instant expiresAt,
                AccessContext accessContext
        ) {
        }

        @Override
        public Optional<SessionDetails> findByTokenHash(String tokenHash) {
            return Optional.empty();
        }

        @Override
        public void revokeFamily(String tokenFamilyId) {
        }

        @Override
        public void revokeSession(String tokenHash) {
        }

        @Override
        public void revokeAll(String userId) {
        }

        @Override
        public void revokeByAssignment(String assignmentId) {
            revokedAssignmentIds.add(assignmentId);
        }

        @Override
        public void replaceSession(
                String oldTokenHash,
                String newTokenHash,
                Instant oldRevokedAt
        ) {
        }
    }
}

package com.grab.store.identity.internal.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AccessContext;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.SessionStore;
import com.identity.domain.service.MerchantAccessProfile;
import com.identity.domain.service.MerchantAccountAccessPolicy;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.SessionDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantAccountAccessPolicyTest {
    private final CommonId applicantUserId = new CommonId("applicant-1");
    private final CommonId merchantId = new CommonId("merchant-1");
    private final AccessScope merchantScope = AccessScope.from(
            MerchantAccessProfile.MERCHANT_SCOPE_KEY,
            "merchant-1"
    );
    private final InMemoryAccessAssignments assignments = new InMemoryAccessAssignments();
    private final RecordingSessionStore sessions = new RecordingSessionStore();

    private MerchantAccountAccessPolicy policy;

    @BeforeEach
    void setUp() {
        assignments.values.clear();
        sessions.revokedAssignmentIds.clear();
        policy = new MerchantAccountAccessPolicy(assignments, sessions, fixedIds());
    }

    @Test
    void replaceApplicantWithOwner_withApplicantAccess_shouldRevokeApplicantAndGrantOwner() {
        AccessAssignment applicant = assignment(
                "applicant-assignment",
                MerchantAccessProfile.APPLICANT_ROLE_CODE
        );
        assignments.values.add(applicant);

        AccessAssignment owner = policy.replaceApplicantWithOwner(
                applicantUserId,
                merchantId,
                sellerPlatform()
        );

        assertThat(applicant.getStatus()).isEqualTo(AccessAssignmentStatus.REVOKED);
        assertThat(owner.getRoleCode()).isEqualTo(MerchantAccessProfile.OWNER_ROLE_CODE);
        assertThat(owner.getScope()).isEqualTo(merchantScope);
        assertThat(sessions.revokedAssignmentIds).containsExactly("applicant-assignment");
    }

    @Test
    void replaceApplicantWithOwner_withExistingOwner_shouldBeIdempotent() {
        AccessAssignment owner = assignment("owner-assignment", MerchantAccessProfile.OWNER_ROLE_CODE);
        assignments.values.add(owner);

        AccessAssignment result = policy.replaceApplicantWithOwner(
                applicantUserId,
                merchantId,
                sellerPlatform()
        );

        assertThat(result).isSameAs(owner);
        assertThat(assignments.values).containsExactly(owner);
        assertThat(sessions.revokedAssignmentIds).isEmpty();
    }

    private AccessAssignment assignment(String id, String roleCode) {
        Instant now = Instant.parse("2026-07-02T00:00:00Z");
        return new AccessAssignment(
                new CommonId(id),
                applicantUserId,
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                roleCode,
                merchantScope,
                AccessAssignmentStatus.ACTIVE,
                null,
                now,
                now,
                null
        );
    }

    private Platform sellerPlatform() {
        return new Platform(
                new CommonId("seller-platform"),
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                "Seller Portal",
                true,
                Set.of(
                        MerchantAccessProfile.APPLICANT_ROLE_CODE,
                        MerchantAccessProfile.OWNER_ROLE_CODE
                )
        );
    }

    private IdGenerator fixedIds() {
        return new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("owner-assignment");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
    }

    private static final class InMemoryAccessAssignments implements AccessAssignmentRepository {
        private final List<AccessAssignment> values = new ArrayList<>();

        @Override
        public Optional<AccessAssignment> findById(Id id) {
            return values.stream().filter(value -> value.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<AccessAssignment> findCurrent(
                Id userId,
                String platformCode,
                String roleCode,
                AccessScope scope
        ) {
            return values.stream()
                    .filter(value -> value.getUserId().equals(userId))
                    .filter(value -> value.getPlatformCode().equals(platformCode))
                    .filter(value -> value.getRoleCode().equals(roleCode))
                    .filter(value -> value.getScope().equals(scope))
                    .filter(value -> value.getStatus() == AccessAssignmentStatus.ACTIVE
                            || value.getStatus() == AccessAssignmentStatus.SUSPENDED)
                    .findFirst();
        }

        @Override
        public List<AccessAssignment> findEffectiveByUserAndPlatform(Id userId, String platformCode, Instant now) {
            return List.of();
        }

        @Override
        public List<AccessAssignment> findByUser(Id userId) {
            return List.copyOf(values);
        }

        @Override
        public boolean existsEffective(
                Id userId,
                String platformCode,
                String roleCode,
                AccessScope scope,
                Instant now
        ) {
            return findCurrent(userId, platformCode, roleCode, scope).isPresent();
        }

        @Override
        public boolean existsCurrent(Id userId, String platformCode, String roleCode, AccessScope scope) {
            return findCurrent(userId, platformCode, roleCode, scope).isPresent();
        }

        @Override
        public AccessAssignment save(AccessAssignment assignment) {
            values.removeIf(value -> value.getId().equals(assignment.getId()));
            values.add(assignment);
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
        public void replaceSession(String oldTokenHash, String newTokenHash, Instant oldRevokedAt) {
        }
    }
}

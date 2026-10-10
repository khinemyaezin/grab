package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RolePermissionReference;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.application.model.write.RegisterRoleDeclarationResult;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.RoleDeclarationRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.security.CatalogAuthority;
import com.identity.domain.security.CatalogModule;
import com.identity.domain.security.CatalogScope;
import com.identity.domain.security.RoleDeclarationCandidate;
import com.identity.domain.security.RoleDeclarationCandidateStatus;
import com.identity.domain.security.RoleDeclarationReceipt;
import com.identity.domain.security.RoleDeclarationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRoleDeclarationServiceTest {
    private final FakeSecurityCatalogRepository catalogs = new FakeSecurityCatalogRepository();
    private final FakeRoleRepository roles = new FakeRoleRepository();
    private final FakeAuthorityRepository authorities = new FakeAuthorityRepository();
    private final FakeRoleDeclarationRepository declarations = new FakeRoleDeclarationRepository();
    private RegisterRoleDeclarationService service;

    @BeforeEach
    void setUp() {
        catalogs.catalog = catalogWithPermissionDependencies();
        service = new RegisterRoleDeclarationService(catalogs, roles, authorities, declarations, new TestIdGenerator());
    }

    @Test
    void executeWhenDependenciesAreReadyAppliesRoleAndPermissions() {
        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM,
                true, false, Set.of());
        roles.save(role);
        Authority authority = Authority.create(new CommonId("a-1"), "CATALOG_READ", "catalog", "Catalog Read", "Read");
        authorities.activeAuthorities = Set.of(authority);

        RegisterRoleDeclarationResult result =
                service.execute(new RegisterRoleDeclarationCommand(declaration(), "event-1"));

        assertThat(result.outcome()).isEqualTo(RoleDeclarationCandidateStatus.APPLIED.name());
        assertThat(role.isActive()).isTrue();
        assertThat(role.isAssignable()).isTrue();
        assertThat(role.getAuthorities()).containsExactly(authority);
        assertThat(declarations.states.get("MERCHANT_ADMIN").appliedRevision()).isEqualTo(1);
        assertThat(declarations.candidates.values()).extracting(RoleDeclarationCandidate::status)
                .containsExactly(RoleDeclarationCandidateStatus.APPLIED);
        assertThat(declarations.receipts.get("event-1").status()).isEqualTo(RoleDeclarationCandidateStatus.APPLIED);
    }

    @Test
    void executeWithDuplicatePublishedRevisionDoesNotPersistDuplicateCandidateOrReceipt() {
        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM,
                true, false, Set.of());
        roles.save(role);
        Authority authority = Authority.create(new CommonId("a-1"), "CATALOG_READ", "catalog", "Catalog Read", "Read");
        authorities.activeAuthorities = Set.of(authority);
        RoleDeclaration declaration = declaration();

        RegisterRoleDeclarationResult first =
                service.execute(new RegisterRoleDeclarationCommand(declaration, "event-1"));
        RegisterRoleDeclarationResult duplicate =
                service.execute(new RegisterRoleDeclarationCommand(declaration, "event-2"));

        assertThat(first.outcome()).isEqualTo(RoleDeclarationCandidateStatus.APPLIED.name());
        assertThat(duplicate.outcome()).isEqualTo(RoleDeclarationCandidateStatus.APPLIED.name());
        assertThat(declarations.candidates).hasSize(1);
        assertThat(declarations.receipts).containsOnlyKeys("event-1");
    }

    @Test
    void executeWhenDependenciesAreMissingPersistsDeclarationButKeepsRoleUnassignable() {
        CatalogScope scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        catalogs.catalog = SecurityCatalog.rehydrate(new CommonId("cat-1"), 1L, List.of(scope), List.of(), List.of());
        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM,
                true, false, Set.of());
        roles.save(role);

        RegisterRoleDeclarationResult result =
                service.execute(new RegisterRoleDeclarationCommand(declaration(), "event-1"));

        assertThat(result.outcome()).isEqualTo(RoleDeclarationCandidateStatus.WAITING_DEPENDENCY.name());
        assertThat(role.isActive()).isFalse();
        assertThat(role.isAssignable()).isFalse();
        assertThat(declarations.candidates.values()).extracting(RoleDeclarationCandidate::status)
                .containsExactly(RoleDeclarationCandidateStatus.WAITING_DEPENDENCY);
        assertThat(declarations.receipts.get("event-1").status())
                .isEqualTo(RoleDeclarationCandidateStatus.WAITING_DEPENDENCY);
    }

    @Test
    void executeWithInvalidDigestQuarantinesWithoutLoadingCatalog() {
        RegisterRoleDeclarationCommand command = new RegisterRoleDeclarationCommand(
                declaration(), "event-1", "invalid-digest", null);

        RegisterRoleDeclarationResult result = service.execute(command);

        assertThat(result.outcome()).isEqualTo(RoleDeclarationCandidateStatus.QUARANTINED.name());
        assertThat(declarations.conflicts).hasSize(1);
        assertThat(catalogs.loadCount).isZero();
        assertThat(declarations.receipts.get("event-1").status()).isEqualTo(RoleDeclarationCandidateStatus.QUARANTINED);
    }

    @Test
    void revalidationAppliesWaitingDeclarationAndUpdatesInboxStatus() {
        CatalogScope scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        catalogs.catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(), List.of());
        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM,
                true, false, Set.of());
        roles.save(role);
        RoleDeclaration declaration = declaration();
        RegisterRoleDeclarationCommand originalCommand = new RegisterRoleDeclarationCommand(declaration, "event-1");
        RegisterRoleDeclarationResult waiting = service.execute(originalCommand);
        assertThat(waiting.outcome()).isEqualTo(RoleDeclarationCandidateStatus.WAITING_DEPENDENCY.name());

        catalogs.catalog = catalogWithPermissionDependencies();
        Authority authority = Authority.create(new CommonId("a-1"), "CATALOG_READ", "catalog", "Catalog Read", "Read");
        authorities.activeAuthorities = Set.of(authority);
        RegisterRoleDeclarationCommand revalidation = new RegisterRoleDeclarationCommand(
                declaration, "event-1", declaration.contentDigest(), null, true);

        RegisterRoleDeclarationResult applied = service.execute(revalidation);

        assertThat(applied.outcome()).isEqualTo(RoleDeclarationCandidateStatus.APPLIED.name());
        assertThat(declarations.receipts.get("event-1").status()).isEqualTo(RoleDeclarationCandidateStatus.APPLIED);
        assertThat(role.isAssignable()).isTrue();
    }

    @Test
    void revalidationSupersedesWaitingRevisionAfterNewerRevisionIsAccepted() {
        CatalogScope scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        catalogs.catalog = SecurityCatalog.rehydrate(
                new CommonId("cat-1"), 1L, List.of(scope), List.of(), List.of());
        Role role = Role.rehydrate(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM,
                true, false, Set.of());
        roles.save(role);
        RoleDeclaration revisionOne = declaration(1);
        RegisterRoleDeclarationResult waiting = service.execute(
                new RegisterRoleDeclarationCommand(revisionOne, "event-1"));
        assertThat(waiting.outcome()).isEqualTo(RoleDeclarationCandidateStatus.WAITING_DEPENDENCY.name());

        catalogs.catalog = catalogWithPermissionDependencies();
        Authority authority = Authority.create(new CommonId("a-1"), "CATALOG_READ", "catalog", "Catalog Read", "Read");
        authorities.activeAuthorities = Set.of(authority);
        RoleDeclaration revisionTwo = declaration(2);
        RegisterRoleDeclarationResult applied = service.execute(
                new RegisterRoleDeclarationCommand(revisionTwo, "event-2"));
        RegisterRoleDeclarationResult superseded = service.execute(new RegisterRoleDeclarationCommand(
                revisionOne, "event-1", revisionOne.contentDigest(), null, true));

        assertThat(applied.outcome()).isEqualTo(RoleDeclarationCandidateStatus.APPLIED.name());
        assertThat(superseded.outcome()).isEqualTo(RoleDeclarationCandidateStatus.SUPERSEDED.name());
        assertThat(declarations.states.get("MERCHANT_ADMIN").appliedRevision()).isEqualTo(2);
        assertThat(declarations.receipts.get("event-1").status()).isEqualTo(RoleDeclarationCandidateStatus.SUPERSEDED);
        assertThat(role.isAssignable()).isTrue();
    }

    private SecurityCatalog catalogWithPermissionDependencies() {
        CatalogScope scope = new CatalogScope("merchant.account", "merchant", null, Lifecycle.ACTIVE, true, 1);
        CatalogAuthority authority = new CatalogAuthority("CATALOG_READ", "catalog", Lifecycle.ACTIVE, true, 1);
        CatalogModule module = new CatalogModule("catalog", 1, "digest");
        return SecurityCatalog.rehydrate(new CommonId("cat-1"), 1L, List.of(scope), List.of(authority), List.of(module));
    }

    private RoleDeclaration declaration() {
        return declaration(1);
    }

    private RoleDeclaration declaration(int revision) {
        return new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", revision, Map.of("catalog", 1),
                List.of(new RolePermissionReference("catalog", "CATALOG_READ")));
    }

    private static final class TestIdGenerator implements IdGenerator {
        @Override
        public Id generateId() {
            return new CommonId("generated-id");
        }

        @Override
        public Id convertIdFrom(String id) {
            return new CommonId(id);
        }
    }

    private static final class FakeSecurityCatalogRepository implements SecurityCatalogRepository {
        private SecurityCatalog catalog;
        private int loadCount;

        @Override
        public boolean ensureInitialized(boolean creationAllowed) {
            return false;
        }

        @Override
        public SecurityCatalog loadForUpdate() {
            loadCount++;
            return catalog;
        }

        @Override
        public void save(SecurityCatalog value) {
            catalog = value;
        }
    }

    private static final class FakeRoleRepository implements RoleRepository {
        private final Map<String, Role> roles = new HashMap<>();

        @Override
        public Optional<Role> findByCode(String code) {
            return Optional.ofNullable(roles.get(code));
        }

        @Override
        public Set<Role> findByCodes(Set<String> codes) {
            return codes.stream().map(roles::get).filter(Objects::nonNull).collect(Collectors.toSet());
        }

        @Override
        public Role save(Role role) {
            roles.put(role.getCode(), role);
            return role;
        }
    }

    private static final class FakeAuthorityRepository implements AuthorityRepository {
        private Set<Authority> activeAuthorities = Set.of();

        @Override
        public Set<Authority> findActiveByCodes(Set<String> codes) {
            return activeAuthorities.stream().filter(authority -> codes.contains(authority.getCode()))
                    .collect(Collectors.toSet());
        }

        @Override
        public void upsertAll(List<Authority> values) {
            activeAuthorities = Set.copyOf(values);
        }
    }

    private static final class FakeRoleDeclarationRepository implements RoleDeclarationRepository {
        private final Map<CandidateKey, RoleDeclarationCandidate> candidates = new HashMap<>();
        private final Map<String, RoleDeclarationReceipt> receipts = new HashMap<>();
        private final Map<String, RoleDeclarationState> states = new HashMap<>();
        private final List<String> conflicts = new ArrayList<>();

        @Override
        public Optional<RoleDeclarationCandidate> findCandidate(String owner, String roleCode, int revision) {
            return Optional.ofNullable(candidates.get(new CandidateKey(owner, roleCode, revision)));
        }

        @Override
        public int highestAcceptedRevision(String owner, String roleCode) {
            return candidates.values().stream()
                    .filter(candidate -> candidate.declaration().owner().equals(owner))
                    .filter(candidate -> candidate.declaration().roleCode().equals(roleCode))
                    .filter(candidate -> candidate.status() == RoleDeclarationCandidateStatus.RECEIVED
                            || candidate.status() == RoleDeclarationCandidateStatus.WAITING_DEPENDENCY
                            || candidate.status() == RoleDeclarationCandidateStatus.APPLIED)
                    .mapToInt(candidate -> candidate.declaration().declarationRevision())
                    .max()
                    .orElse(0);
        }

        @Override
        public void saveCandidate(RoleDeclarationCandidate candidate) {
            CandidateKey key = new CandidateKey(candidate.declaration().owner(), candidate.declaration().roleCode(),
                    candidate.declaration().declarationRevision());
            candidates.put(key, candidate);
        }

        @Override
        public Optional<RoleDeclarationReceipt> findReceipt(String eventId) {
            return Optional.ofNullable(receipts.get(eventId));
        }

        @Override
        public void saveReceipt(RoleDeclarationReceipt receipt) {
            receipts.put(receipt.eventId(), receipt);
        }

        @Override
        public Optional<RoleDeclarationState> findState(String roleCode) {
            return Optional.ofNullable(states.get(roleCode));
        }

        @Override
        public void saveState(RoleDeclarationState state) {
            states.put(state.roleCode(), state);
        }

        @Override
        public void recordConflict(
                String eventId,
                String owner,
                String roleCode,
                int revision,
                String suppliedDigest,
                String existingDigest,
                String payload,
                String reason
        ) {
            conflicts.add(eventId + ":" + reason);
        }
    }

    private record CandidateKey(String owner, String roleCode, int revision) {
    }
}

package com.identity.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.identity.application.model.read.AccessAssignmentView;
import com.identity.application.model.read.GetUserAccessContextsQuery;
import com.identity.application.model.read.ScopeCatalogView;
import com.identity.application.model.read.UserAccessContextsResult;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.Role;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.port.outbound.RoleRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class GetUserAccessContextsServiceTest {

    @Test
    void execute_resolvesContextsWithGroupedAuthoritiesAndDisplayNames() {
        Instant now = Instant.parse("2026-10-11T00:00:00Z");
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put("merchant.account", null);
        parents.put("merchant.storefront", "merchant.account");
        ScopeCatalogView catalog = new ScopeCatalogView(
                parents,
                Map.of("merchant.account", "merchant", "merchant.storefront", "merchant"),
                Set.of()
        );
        ScopeCatalogQueryPort scopes = () -> catalog;

        AccessAssignmentView assignment1 = new AccessAssignmentView(
                "assign-1", "user-1", "MERCHANT_ADMIN", "merchant.account", "merchant-123",
                AccessAssignmentStatus.ACTIVE, AccessAssignmentStatus.ACTIVE, "admin",
                Instant.EPOCH, Instant.EPOCH, null, "Acme Merchant", "Primary Account"
        );
        AccessAssignmentView assignment2 = new AccessAssignmentView(
                "assign-2", "user-1", "STORE_MANAGER", "merchant.storefront", "store-456",
                AccessAssignmentStatus.ACTIVE, AccessAssignmentStatus.ACTIVE, "admin",
                Instant.EPOCH, Instant.EPOCH, null, "Acme Downtown Store", "Storefront"
        );

        AccessAssignmentQueryPort assignments = new AccessAssignmentQueryPort() {
            @Override
            public List<AccessAssignmentView> findByUser(String userId) {
                return List.of();
            }

            @Override
            public List<AccessAssignmentView> findEffectiveByUser(String userId, Instant queriedAt) {
                return List.of(assignment1, assignment2);
            }
        };

        Authority auth1 = Authority.create(new CommonId("auth-1"), "ORDER_READ", "order", "Read orders", "desc");
        Authority auth2 = Authority.create(new CommonId("auth-2"), "ORDER_WRITE", "order", "Write orders", "desc");
        Authority auth3 = Authority.create(new CommonId("auth-3"), "CATALOG_READ", "catalog", "Read catalog", "desc");

        Role roleAdmin = new Role(
                new CommonId("role-admin"), "MERCHANT_ADMIN", "Admin", "Desc", RoleKind.SYSTEM, true, true, Set.of(auth1, auth2)
        );
        Role roleManager = new Role(
                new CommonId("role-manager"), "STORE_MANAGER", "Manager", "Desc", RoleKind.SYSTEM, true, true, Set.of(auth3)
        );

        RoleRepository roles = new RoleRepository() {
            @Override
            public Optional<Role> findByCode(String code) {
                return Optional.empty();
            }

            @Override
            public Set<Role> findByCodes(Set<String> codes) {
                Set<Role> matched = new HashSet<>();
                if (codes.contains("MERCHANT_ADMIN")) matched.add(roleAdmin);
                if (codes.contains("STORE_MANAGER")) matched.add(roleManager);
                return matched;
            }

            @Override
            public Role save(Role role) {
                return role;
            }
        };

        GetUserAccessContextsService service = new GetUserAccessContextsService(scopes, assignments, roles);
        UserAccessContextsResult result = service.execute(new GetUserAccessContextsQuery(new CommonId("user-1")));

        assertThat(result.userId()).isEqualTo(new CommonId("user-1"));
        assertThat(result.contexts()).hasSize(2);

        var ctx1 = result.contexts().stream().filter(c -> c.scopeKey().equals("merchant.account")).findFirst().orElseThrow();
        assertThat(ctx1.assignmentId()).isEqualTo("assign-1");
        assertThat(ctx1.scopeId()).isEqualTo("merchant-123");
        assertThat(ctx1.role()).isEqualTo("MERCHANT_ADMIN");
        assertThat(ctx1.displayName()).isEqualTo("Acme Merchant");
        assertThat(ctx1.authorities()).containsExactlyInAnyOrder("ORDER_READ", "ORDER_WRITE");

        var ctx2 = result.contexts().stream().filter(c -> c.scopeKey().equals("merchant.storefront")).findFirst().orElseThrow();
        assertThat(ctx2.assignmentId()).isEqualTo("assign-2");
        assertThat(ctx2.scopeId()).isEqualTo("store-456");
        assertThat(ctx2.displayName()).isEqualTo("Acme Downtown Store");
        assertThat(ctx2.authorities()).containsExactly("CATALOG_READ");
    }
}

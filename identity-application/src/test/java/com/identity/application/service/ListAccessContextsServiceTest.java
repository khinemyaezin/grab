package com.identity.application.service;

import com.grab.framework.id.impl.CommonId;
import com.identity.application.model.read.AccessAssignmentView;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.model.read.ScopeCatalogView;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
import com.identity.domain.enums.AccessAssignmentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ListAccessContextsServiceTest {
    private final ScopeCatalogQueryPort scopes = () -> catalog();

    @Test
    void execute_groupsRolesAndCarriesAnchorDisplayForExactScope() {
        Instant now = Instant.parse("2026-10-11T00:00:00Z");
        AccessAssignmentView accountAdmin = assignment("assignment-1", "MERCHANT_ADMIN", "merchant.account", "same-id",
                "Shop", null, null);
        AccessAssignmentView accountViewer = assignment("assignment-2", "MERCHANT_VIEWER", "merchant.account", "same-id",
                "Shop", null, now.plusSeconds(30));
        AccessAssignmentView storefrontAdmin = assignment("assignment-3", "STORE_ADMIN", "merchant.storefront", "same-id",
                "Downtown", "downtown", now.plusSeconds(60));
        AccessAssignmentQueryPort assignments = new AccessAssignmentQueryPort() {
            @Override
            public List<AccessAssignmentView> findByUser(String userId) {
                return List.of();
            }

            @Override
            public List<AccessAssignmentView> findEffectiveByUser(String userId, Instant queriedAt) {
                assertThat(userId).isEqualTo("user-1");
                assertThat(queriedAt).isNotNull();
                return List.of(accountAdmin, accountViewer, storefrontAdmin);
            }
        };

        List<com.identity.application.model.read.AccessContextResult> result =
                new ListAccessContextsService(scopes, assignments)
                        .execute(new ListAccessContextsQuery(new CommonId("user-1")));

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().assignmentId()).isEqualTo("assignment-1");
        assertThat(result.getFirst().roleCodes()).containsExactlyInAnyOrder("MERCHANT_ADMIN", "MERCHANT_VIEWER");
        assertThat(result.getFirst().expiresAt()).isNull();
        assertThat(result.getFirst().title()).isEqualTo("Shop");
        assertThat(result.getFirst().subtitle()).isNull();
        assertThat(result.get(1).scopeKey()).isEqualTo("merchant.storefront");
        assertThat(result.get(1).title()).isEqualTo("Downtown");
        assertThat(result.get(1).subtitle()).isEqualTo("downtown");
    }

    private AccessAssignmentView assignment(
            String id, String roleCode, String scopeKey, String scopeId,
            String title, String subtitle, Instant expiresAt
    ) {
        return new AccessAssignmentView(id, "user-1", roleCode, scopeKey, scopeId,
                AccessAssignmentStatus.ACTIVE, AccessAssignmentStatus.ACTIVE, "operator",
                Instant.EPOCH, Instant.EPOCH, expiresAt, title, subtitle);
    }

    private ScopeCatalogView catalog() {
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put("merchant.account", null);
        parents.put("merchant.storefront", "merchant.account");
        return new ScopeCatalogView(parents,
                Map.of("merchant.account", "merchant", "merchant.storefront", "merchant"), Set.of());
    }
}

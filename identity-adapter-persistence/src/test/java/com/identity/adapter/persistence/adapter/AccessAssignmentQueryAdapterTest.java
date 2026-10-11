package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.entity.AccessAssignmentEntity;
import com.identity.adapter.persistence.entity.RoleEntity;
import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.application.model.read.ScopeDisplayView;
import com.identity.adapter.persistence.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.adapter.persistence.repository.jpa.ScopeDisplayJpaRepository;
import com.identity.application.model.read.AccessAssignmentView;
import com.identity.domain.enums.AccessAssignmentStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccessAssignmentQueryAdapterTest {
    private List<AccessAssignmentEntity> assignmentRows = List.of();
    private List<ScopeDisplayView> displayRows = List.of();
    private List<String> requestedIds;
    private int displayLookupCount;

    private final AccessAssignmentJpaRepository assignments = proxy(AccessAssignmentJpaRepository.class,
            (methodName, arguments) -> methodName.equals("findEffectiveByUser") ? assignmentRows : null);
    private final ScopeDisplayJpaRepository displays = proxy(ScopeDisplayJpaRepository.class, (methodName, arguments) -> {
        if (methodName.equals("findDisplays")) {
            displayLookupCount++;
            requestedIds = (List<String>) arguments[0];
            return displayRows;
        }
        return null;
    });
    private final AccessAssignmentQueryAdapter adapter = new AccessAssignmentQueryAdapter(
            assignments, displays, new IdentityPersistenceExecutor());

    @Test
    void findEffectiveByUser_enrichesExactScopePairsWithOneBulkLookup() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        assignmentRows = List.of(
                assignment("account-id", "MERCHANT_ADMIN", "merchant.account", "account-scope-id"),
                assignment("store-id", "STORE_ADMIN", "merchant.storefront", "store-scope-id"));
        displayRows = List.of(
                new ScopeDisplayView("account-scope-id", "Merchant", "Legal"),
                new ScopeDisplayView("store-scope-id", "Store", "store-slug"));

        List<AccessAssignmentView> result = adapter.findEffectiveByUser("user-1", now);

        assertThat(result).extracting("title").containsExactly("Merchant", "Store");
        assertThat(result).extracting("subtitle").containsExactly("Legal", "store-slug");
        assertThat(result).extracting("effectiveStatus").containsOnly(AccessAssignmentStatus.ACTIVE);
        assertThat(displayLookupCount).isEqualTo(1);
        assertThat(requestedIds).containsExactly("account-scope-id", "store-scope-id");
    }

    @Test
    void findEffectiveByUser_emptyAssignmentsSkipsDisplayLookup() {
        assignmentRows = List.of();

        assertThat(adapter.findEffectiveByUser("user-1", Instant.EPOCH)).isEmpty();

        assertThat(displayLookupCount).isZero();
    }

    private AccessAssignmentEntity assignment(String uuid, String roleCode, String scopeKey, String scopeId) {
        UserEntity user = new UserEntity();
        user.setUuid("user-1");
        RoleEntity role = new RoleEntity();
        role.setCode(roleCode);
        AccessAssignmentEntity assignment = new AccessAssignmentEntity();
        assignment.setUuid(uuid);
        assignment.setUser(user);
        assignment.setRole(role);
        assignment.setScopeKey(scopeKey);
        assignment.setScopeId(scopeId);
        assignment.setStatus(AccessAssignmentStatus.ACTIVE);
        assignment.setCreatedAt(Instant.EPOCH);
        assignment.setUpdatedAt(Instant.EPOCH);
        return assignment;
    }

    private <T> T proxy(Class<T> type, RepositoryCall call) {
        Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (instance, method, arguments) -> {
            return call.invoke(method.getName(), arguments == null ? new Object[0] : arguments);
        });
        return type.cast(proxy);
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String methodName, Object[] arguments);
    }
}

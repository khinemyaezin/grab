package com.grab.store.identity.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.identity.internal.query.ListAccessContextsQuery;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.ScopeKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAccessContextsQueryHandlerTest {

    @Mock
    private AccessAssignmentRepository assignments;

    @Test
    void handle_shouldGroupRolesByPlatformAndScope() {
        CommonId userId = new CommonId("user-1");
        when(assignments.findEffectiveByUserAndPlatform(
                eq(userId), eq("SELLER_PORTAL"), any(Instant.class)
        )).thenReturn(List.of(
                assignment("assignment-1", "MERCHANT_OWNER", "merchant-1"),
                assignment("assignment-2", "STORE_MANAGER", "merchant-1"),
                assignment("assignment-3", "MERCHANT_OWNER", "merchant-2")
        ));

        var results = new ListAccessContextsQueryHandler(assignments)
                .handle(new ListAccessContextsQuery(userId, "SELLER_PORTAL"));

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().assignmentId()).isEqualTo("assignment-1");
        assertThat(results.getFirst().scopeId()).isEqualTo("merchant-1");
        assertThat(results.getFirst().roleCodes())
                .containsExactlyInAnyOrder("MERCHANT_OWNER", "STORE_MANAGER");
        assertThat(results.get(1).scopeId()).isEqualTo("merchant-2");
        assertThat(results.get(1).roleCodes()).containsExactly("MERCHANT_OWNER");
    }

    private AccessAssignment assignment(String id, String roleCode, String scopeId) {
        Instant now = Instant.now();
        return new AccessAssignment(
                new CommonId(id),
                new CommonId("user-1"),
                "SELLER_PORTAL",
                roleCode,
                new AccessScope(new ScopeKey("merchant.account"), scopeId),
                AccessAssignmentStatus.ACTIVE,
                new CommonId("admin-1"),
                now,
                now,
                null
        );
    }
}

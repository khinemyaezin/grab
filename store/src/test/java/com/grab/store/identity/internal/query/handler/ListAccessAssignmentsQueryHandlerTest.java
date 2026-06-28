package com.grab.store.identity.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.identity.internal.query.ListAccessAssignmentsQuery;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.ScopeKey;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListAccessAssignmentsQueryHandlerTest {
    private final AccessAssignmentRepository assignments = mock(AccessAssignmentRepository.class);
    private final ListAccessAssignmentsQueryHandler handler = new ListAccessAssignmentsQueryHandler(assignments);

    @Test
    void handle_withMerchantScope_shouldHideAssignmentsFromOtherMerchants() {
        var userId = new CommonId("user-1");
        when(assignments.findByUser(userId)).thenReturn(List.of(
                assignment("assignment-1", userId, "merchant-1"),
                assignment("assignment-2", userId, "merchant-2")
        ));

        var results = handler.handle(new ListAccessAssignmentsQuery(
                userId, "merchant.account", "merchant-1"
        ));

        assertThat(results).extracting(result -> result.scopeId())
                .containsExactly("merchant-1");
    }

    @Test
    void handle_withGlobalScope_shouldReturnAllAssignments() {
        var userId = new CommonId("user-1");
        when(assignments.findByUser(userId)).thenReturn(List.of(
                assignment("assignment-1", userId, "merchant-1"),
                assignment("assignment-2", userId, "merchant-2")
        ));

        var results = handler.handle(new ListAccessAssignmentsQuery(userId, "global", "*"));

        assertThat(results).hasSize(2);
    }

    private AccessAssignment assignment(String id, CommonId userId, String merchantId) {
        Instant now = Instant.now();
        return new AccessAssignment(
                new CommonId(id),
                userId,
                "SELLER_PORTAL",
                "MERCHANT_ADMIN",
                new AccessScope(new ScopeKey("merchant.account"), merchantId),
                AccessAssignmentStatus.ACTIVE,
                new CommonId("owner-1"),
                now,
                now,
                null
        );
    }
}

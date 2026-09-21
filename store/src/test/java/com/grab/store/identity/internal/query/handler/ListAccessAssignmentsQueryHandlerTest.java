package com.grab.store.identity.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.application.model.read.AccessAssignmentView;
import com.identity.application.service.ListAccessAssignmentsService;
import com.identity.domain.enums.AccessAssignmentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListAccessAssignmentsServiceTest {
    private final AccessAssignmentQueryPort assignments = mock(AccessAssignmentQueryPort.class);
    private final ListAccessAssignmentsService handler = new ListAccessAssignmentsService(assignments);

    @Test
    void handle_withMerchantScope_shouldHideAssignmentsFromOtherMerchants() {
        var userId = new CommonId("user-1");
        when(assignments.findByUser("user-1")).thenReturn(List.of(
                assignment("assignment-1", "user-1", "merchant-1"),
                assignment("assignment-2", "user-1", "merchant-2")
        ));

        var results = handler.execute(new ListAccessAssignmentsQuery(
                userId, "merchant.account", "merchant-1"
        ));

        assertThat(results).extracting(result -> result.scopeId())
                .containsExactly("merchant-1");
    }

    @Test
    void handle_withGlobalScope_shouldReturnAllAssignments() {
        var userId = new CommonId("user-1");
        when(assignments.findByUser("user-1")).thenReturn(List.of(
                assignment("assignment-1", "user-1", "merchant-1"),
                assignment("assignment-2", "user-1", "merchant-2")
        ));

        var results = handler.execute(new ListAccessAssignmentsQuery(userId, "global", "*"));

        assertThat(results).hasSize(2);
    }

    private AccessAssignmentView assignment(String id, String userId, String merchantId) {
        Instant now = Instant.now();
        return new AccessAssignmentView(
                id,
                userId,
                "SELLER_PORTAL",
                "MERCHANT_ADMIN",
                "merchant.account",
                merchantId,
                AccessAssignmentStatus.ACTIVE,
                AccessAssignmentStatus.ACTIVE,
                "owner-1",
                now,
                now,
                null
        );
    }
}

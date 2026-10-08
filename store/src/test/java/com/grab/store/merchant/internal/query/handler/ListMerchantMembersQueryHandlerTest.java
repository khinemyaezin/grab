package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.ListMerchantMembersUseCase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListMerchantMembersQueryHandlerTest {

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @Test
    void handle_shouldDelegateToUseCase() {
        ListMerchantMembersUseCase useCase = mock(ListMerchantMembersUseCase.class);
        ListMerchantMembersQueryHandler handler = new ListMerchantMembersQueryHandler(useCase);

        ListMerchantMembersQuery query = new ListMerchantMembersQuery(new CommonId("mer-1"));
        List<MerchantMemberResult> expected = List.of(
                new MerchantMemberResult("mem-1", "mer-1", "usr-1", "MERCHANT_ADMIN", "ACTIVE", null, null, now, now, now, 0L)
        );
        when(useCase.execute(query)).thenReturn(expected);

        List<MerchantMemberResult> actual = handler.handle(query);

        assertThat(actual).isEqualTo(expected);
        assertThat(handler.getQueryType()).isEqualTo(ListMerchantMembersQuery.class);
        verify(useCase).execute(query);
    }
}

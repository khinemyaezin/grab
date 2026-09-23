package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.grab.store.merchant.internal.api.rest.mapper.ListMerchantMembersRequestMapper;
import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MerchantMemberQueryServiceTest {

    private QueryBus queries;
    private ListMerchantMembersRequestMapper listMapper;
    private MerchantMemberQueryService service;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        queries = mock(QueryBus.class);
        listMapper = mock(ListMerchantMembersRequestMapper.class);
        service = new MerchantMemberQueryService(queries, listMapper);
    }

    @Test
    void list_shouldMapAndDispatchQuery() {
        ListMerchantMembersQuery query = new ListMerchantMembersQuery(new CommonId("mer-1"));
        List<MerchantMemberResult> results = List.of(
                new MerchantMemberResult("mem-1", "mer-1", "usr-1", "MERCHANT_ADMIN", "ACTIVE", null, null, now, now, now, 0L)
        );
        List<MerchantMemberResponse> responses = List.of(
                new MerchantMemberResponse("mem-1", "mer-1", "usr-1", "MERCHANT_ADMIN", "ACTIVE", null, null, now, now, now, 0L)
        );

        when(listMapper.toQuery("mer-1")).thenReturn(query);
        when(queries.dispatch(query)).thenReturn(results);
        when(listMapper.toResponse(results)).thenReturn(responses);

        List<MerchantMemberResponse> actual = service.list("mer-1");

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).role()).isEqualTo("MERCHANT_ADMIN");
        verify(queries).dispatch(query);
    }
}

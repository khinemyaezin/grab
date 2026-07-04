package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.api.rest.mapper.GetMerchantRequestMapper;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.query.GetMerchantQuery;
import com.grab.store.shared.security.SecurityPrincipal;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantQueryServiceTest {
    @Test
    void current_withSelectedMerchantContext_shouldDispatchScopedMerchantQuery() {
        AtomicReference<Query<?>> dispatched = new AtomicReference<>();
        QueryBus queryBus = new QueryBus() {
            @Override
            public <R> R dispatch(Query<R> query) {
                dispatched.set(query);
                return null;
            }
        };
        MerchantResponse expected = merchantResponse();
        GetMerchantRequestMapper mapper = new GetMerchantRequestMapper() {
            @Override
            public GetMerchantQuery toQuery(
                    String merchantId,
                    String actorId,
                    boolean reviewerAccess,
                    boolean scopedAccess
            ) {
                return new GetMerchantQuery(
                        new CommonId(merchantId),
                        new CommonId(actorId),
                        reviewerAccess,
                        scopedAccess
                );
            }

            @Override
            public MerchantResponse toResponse(MerchantAccountResult result) {
                return expected;
            }
        };
        MerchantQueryService service = new MerchantQueryService(
                queryBus,
                mapper,
                null,
                null,
                null,
                null,
                new AuthenticatedMerchantScopeResolver()
        );

        MerchantResponse response = service.current(principal());

        GetMerchantQuery query = (GetMerchantQuery) dispatched.get();
        assertThat(query.merchantId().getValue()).isEqualTo("merchant-1");
        assertThat(query.actorId().getValue()).isEqualTo("staff-1");
        assertThat(query.scopedAccess()).isTrue();
        assertThat(response).isSameAs(expected);
    }

    private SecurityPrincipal principal() {
        AccessContext context = new AccessContext(
                "SELLER_PORTAL", "assignment-1", "merchant.account", "merchant-1");
        AuthenticatedActor actor = new AuthenticatedActor(
                "staff-1",
                "local",
                "staff-1",
                "staff@example.com",
                Set.of("STORE_MANAGER"),
                Set.of("MERCHANT_PROFILE_READ"),
                context
        );
        return new SecurityPrincipal(actor);
    }

    private MerchantResponse merchantResponse() {
        return new MerchantResponse(
                "merchant-1",
                "applicant-1",
                "FIRST_PARTY_RETAILER",
                "Acme Incorporated",
                "Acme Store",
                null,
                null,
                null,
                "ACTIVE",
                null,
                null,
                null,
                null,
                null,
                0L
        );
    }
}

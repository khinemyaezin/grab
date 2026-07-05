package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.merchant.internal.query.GetMerchantQuery;
import com.grab.store.merchant.support.MerchantAccountRepositoryStub;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.exception.MerchantDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetMerchantQueryHandlerTest {
    @Test
    void handle_withDifferentApplicantAndNoReviewerAccess_shouldRejectAccess() {
        CommonId merchantId = new CommonId("merchant-1");
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId,
                new CommonId("applicant-1"),
                MerchantType.FIRST_PARTY_RETAILER,
                "Acme Store",
                Instant.parse("2026-06-28T00:00:00Z")
        );
        MerchantAccountRepositoryStub merchants = new MerchantAccountRepositoryStub();
        merchants.save(merchant);
        GetMerchantQueryHandler handler = new GetMerchantQueryHandler(merchants);
        GetMerchantQuery query = new GetMerchantQuery(
                merchantId, new CommonId("applicant-2"), false, false);

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void handle_withMatchingScopedAccess_shouldReturnMerchant() {
        CommonId merchantId = new CommonId("merchant-1");
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId,
                new CommonId("applicant-1"),
                MerchantType.FIRST_PARTY_RETAILER,
                "Acme Store",
                Instant.parse("2026-06-28T00:00:00Z")
        );
        MerchantAccountRepositoryStub merchants = new MerchantAccountRepositoryStub();
        merchants.save(merchant);
        GetMerchantQueryHandler handler = new GetMerchantQueryHandler(merchants);
        GetMerchantQuery query = new GetMerchantQuery(
                merchantId, new CommonId("staff-1"), false, true);

        var result = handler.handle(query);

        assertThat(result.merchantId()).isEqualTo("merchant-1");
    }
}

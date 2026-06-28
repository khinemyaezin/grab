package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.merchant.internal.query.GetMerchantQuery;
import com.grab.store.merchant.support.MerchantAccountRepositoryStub;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.exception.MerchantDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetMerchantQueryHandlerTest {
    @Test
    void handle_withDifferentApplicantAndNoReviewerAccess_shouldRejectAccess() {
        CommonId merchantId = new CommonId("merchant-1");
        MerchantAccount merchant = MerchantAccount.startDraft(
                merchantId,
                new CommonId("applicant-1"),
                MerchantType.RETAILER,
                "Acme Store",
                Instant.parse("2026-06-28T00:00:00Z")
        );
        MerchantAccountRepositoryStub merchants = new MerchantAccountRepositoryStub();
        merchants.save(merchant);
        GetMerchantQueryHandler handler = new GetMerchantQueryHandler(merchants);
        GetMerchantQuery query = new GetMerchantQuery(
                merchantId, new CommonId("applicant-2"), false);

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(MerchantDomainException.class);
    }
}

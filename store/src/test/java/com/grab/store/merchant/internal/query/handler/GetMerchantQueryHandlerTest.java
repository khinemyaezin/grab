package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.id.impl.CommonId;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.model.read.GetMerchantQuery;
import com.merchant.application.model.read.MerchantAccountView;
import com.merchant.application.service.GetMerchantService;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetMerchantServiceTest {
    @Test
    void execute_withDifferentApplicantAndNoReviewerAccess_shouldRejectAccess() {
        CommonId merchantId = new CommonId("merchant-1");
        MerchantAccountQueryPort merchants = mock(MerchantAccountQueryPort.class);
        when(merchants.findById("merchant-1")).thenReturn(Optional.of(view(
                "merchant-1",
                "applicant-1",
                "Acme Store"
        )));
        GetMerchantService service = new GetMerchantService(merchants);
        GetMerchantQuery query = new GetMerchantQuery(
                merchantId, new CommonId("applicant-2"), false, false);

        assertThatThrownBy(() -> service.execute(query))
                .isInstanceOf(MerchantServiceException.class);
    }

    @Test
    void execute_withMatchingScopedAccess_shouldReturnMerchant() {
        CommonId merchantId = new CommonId("merchant-1");
        MerchantAccountQueryPort merchants = mock(MerchantAccountQueryPort.class);
        when(merchants.findById("merchant-1")).thenReturn(Optional.of(view(
                "merchant-1",
                "applicant-1",
                "Acme Store"
        )));
        GetMerchantService service = new GetMerchantService(merchants);
        GetMerchantQuery query = new GetMerchantQuery(
                merchantId, new CommonId("staff-1"), false, true);

        var result = service.execute(query);

        assertThat(result.merchantId()).isEqualTo("merchant-1");
    }

    private MerchantAccountView view(String merchantId, String applicantUserId, String displayName) {
        Instant now = Instant.parse("2026-06-28T00:00:00Z");
        return new MerchantAccountView(
                merchantId,
                applicantUserId,
                MerchantType.FIRST_PARTY_RETAILER,
                displayName,
                displayName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                MerchantStatus.DRAFT,
                null,
                null,
                null,
                now,
                now,
                0L
        );
    }
}

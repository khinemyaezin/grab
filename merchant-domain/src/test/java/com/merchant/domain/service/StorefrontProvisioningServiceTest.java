package com.merchant.domain.service;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.exception.MerchantDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorefrontProvisioningServiceTest {
    private final Instant now = Instant.parse("2026-09-17T00:00:00Z");
    private final StorefrontProvisioningService service = new StorefrontProvisioningService();

    @Test
    void requireOperational_whenMerchantIsActive_shouldPass() {
        MerchantAccount merchant = MerchantAccount.startDraft(
                new CommonId("merchant-1"),
                new CommonId("applicant-1"),
                MerchantType.FIRST_PARTY_RETAILER,
                "Shop",
                now
        );
        merchant.updateProfile(
                new com.merchant.domain.valueobject.MerchantName("Legal Name", "Shop"),
                new com.merchant.domain.valueobject.BusinessRegistration("MM", "REG-1"),
                new com.merchant.domain.valueobject.ContactInformation("owner@example.com", "+959111111111"),
                new com.merchant.domain.valueobject.RegisteredAddress("1 Main", null, "Yangon", "Yangon", "11181", "MM"),
                now
        );
        merchant.submit(new CommonId("applicant-1"), now);
        merchant.approve(new CommonId("reviewer-1"), now);

        assertThatCode(() -> service.requireOperational(merchant)).doesNotThrowAnyException();
    }

    @Test
    void requireOperational_whenMerchantIsDraft_shouldReject() {
        MerchantAccount merchant = MerchantAccount.startDraft(
                new CommonId("merchant-1"),
                new CommonId("applicant-1"),
                MerchantType.C2C_SELLER,
                "Shop",
                now
        );

        assertThatThrownBy(() -> service.requireOperational(merchant))
                .isInstanceOf(MerchantDomainException.class);
    }
}

package com.merchant.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.event.MerchantApprovedEvent;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantAccountTest {
    private final Instant now = Instant.parse("2026-06-28T00:00:00Z");

    @Test
    void startDraft_shouldCreateDraftWithoutCreatingOtherResources() {
        MerchantAccount merchant = draft(MerchantType.FIRST_PARTY_RETAILER);

        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.DRAFT);
        assertThat(merchant.getRegistration()).isNull();
        assertThat(merchant.getEvents().getFirst()).isInstanceOf(MerchantApplicationStartedEvent.class);
    }

    @Test
    void approve_pendingMerchant_shouldActivateAndEmitEvent() {
        MerchantAccount merchant = pendingMerchant();

        merchant.approve(user("reviewer-1"), now.plusSeconds(30));

        assertThat(merchant.isOperational()).isTrue();
        assertThat(merchant.getEvents().getLast()).isInstanceOf(MerchantApprovedEvent.class);
    }

    @Test
    void approve_draftMerchant_shouldRejectTransition() {
        assertThatThrownBy(() -> draft(MerchantType.FIRST_PARTY_RETAILER).approve(user("reviewer-1"), now))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void requestChanges_thenResubmit_shouldReturnToReview() {
        MerchantAccount merchant = pendingMerchant();
        merchant.requestChanges(user("reviewer-1"), new LifecycleReason("Fix address"), now);
        merchant.updateProfile(
                new MerchantName("Legal Name", "Display Name"), registration(), contact(), address(), now
        );

        merchant.submit(user("applicant-1"), now);

        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.PENDING_REVIEW);
    }

    @Test
    void updateProfile_emitsMerchantProfileUpdatedEvent() {
        MerchantAccount merchant = draft(MerchantType.FIRST_PARTY_RETAILER);

        merchant.updateProfile(new MerchantName(null, "Updated Shop"), null, contact(), null, now.plusSeconds(5));

        MerchantProfileUpdatedEvent event = (MerchantProfileUpdatedEvent) merchant.getEvents().getLast();
        assertThat(event.aggregateVersion()).isEqualTo(1L);
        assertThat(event.merchantName()).isEqualTo("Updated Shop");
    }

    @Test
    void rejectedMerchant_shouldBeTerminal() {
        MerchantAccount merchant = pendingMerchant();
        merchant.reject(user("reviewer-1"), new LifecycleReason("Not eligible"), now);

        assertThatThrownBy(() -> merchant.submit(user("applicant-1"), now))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void suspendedMerchant_canReactivateThenClose() {
        MerchantAccount merchant = pendingMerchant();
        merchant.approve(user("reviewer-1"), now);
        merchant.suspend(user("operator-1"), new LifecycleReason("Compliance review"), now);
        merchant.reactivate(user("operator-1"), now);
        merchant.close(user("operator-1"), new LifecycleReason("Requested closure"), now);

        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.CLOSED);
        assertThatThrownBy(() -> merchant.reactivate(user("operator-1"), now))
                .isInstanceOf(MerchantDomainException.class);
    }

    private MerchantAccount pendingMerchant() {
        MerchantAccount merchant = draft(MerchantType.FIRST_PARTY_RETAILER);
        merchant.updateProfile(
                new MerchantName("Legal Name", "Display Name"), registration(), contact(), address(), now
        );
        merchant.submit(user("applicant-1"), now);
        return merchant;
    }

    private MerchantAccount draft(MerchantType type) {
        return MerchantAccount.startDraft(user("merchant-1"), user("applicant-1"), type, "US", "Display Name", now);
    }

    private BusinessRegistration registration() {
        return new BusinessRegistration("MM", "REG-001");
    }

    private ContactInformation contact() {
        return new ContactInformation("owner@example.com", "+959123456789");
    }

    private RegisteredAddress address() {
        return new RegisteredAddress("1 Main Road", null, "Yangon", "Yangon", "11181", "MM");
    }

    private CommonId user(String value) {
        return new CommonId(value);
    }
}

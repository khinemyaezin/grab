package com.grab.store.merchant.internal.event;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import com.grab.store.merchant.support.MerchantAccountRepositoryStub;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.event.MerchantApprovedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantIntegrationEventPublisherTest {
    @Test
    void handleMerchantApproved_shouldPublishPublicIntegrationEvent() {
        AtomicReference<Object> published = new AtomicReference<>();
        MerchantAccountRepositoryStub merchants = new MerchantAccountRepositoryStub();
        merchants.save(MerchantAccount.startDraft(
                new CommonId("merchant-1"),
                new CommonId("applicant-1"),
                MerchantType.FIRST_PARTY_RETAILER,
                "Merchant-1",
                Instant.parse("2026-07-02T00:00:00Z")
        ));
        MerchantIntegrationEventPublisher publisher = publisher(published, merchants);
        Instant occurredAt = Instant.parse("2026-07-02T00:00:00Z");

        publisher.handleMerchantApproved(new MerchantApprovedEvent(
                "merchant-1",
                "Merchant-1",
                "applicant-1",
                "ACTIVE",
                "reviewer-1",
                2,
                occurredAt
        ));

        assertThat(published.get()).isEqualTo(new MerchantApprovedIntegrationEvent(
                "merchant-1",
                "applicant-1",
                "Merchant-1",
                "FIRST_PARTY_RETAILER",
                "ACTIVE",
                occurredAt,
                1
        ));
    }

    @Test
    void handleMerchantSuspended_shouldPublishPublicIntegrationEvent() {
        AtomicReference<Object> published = new AtomicReference<>();
        MerchantIntegrationEventPublisher publisher = publisher(published, new MerchantAccountRepositoryStub());
        Instant occurredAt = Instant.parse("2026-07-02T00:00:00Z");

        publisher.handleMerchantSuspended(new MerchantSuspendedEvent(
                "merchant-1",
                "Merchant-1",
                "applicant-1",
                "SUSPENDED",
                "operator-1",
                3,
                occurredAt
        ));

        assertThat(published.get()).isInstanceOf(com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent.class);
    }

    private MerchantIntegrationEventPublisher publisher(
            AtomicReference<Object> published,
            MerchantAccountRepositoryStub merchants
    ) {
        ApplicationEventPublisher events = published::set;
        return new MerchantIntegrationEventPublisher(events, merchants, new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("generated");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        });
    }
}

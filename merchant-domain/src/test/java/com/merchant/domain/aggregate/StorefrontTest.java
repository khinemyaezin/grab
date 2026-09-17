package com.merchant.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.enums.StorefrontStatus;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontStatusChangedEvent;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.valueobject.LifecycleReason;
import com.merchant.domain.valueobject.StorefrontName;
import com.merchant.domain.valueobject.StorefrontSlug;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorefrontTest {
    private final Instant now = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    void createDraft_shouldStartInDraftAndEmitCreatedEvent() {
        Storefront storefront = draft();

        assertThat(storefront.getStatus()).isEqualTo(StorefrontStatus.DRAFT);
        assertThat(storefront.getEvents().getFirst()).isInstanceOf(StorefrontCreatedEvent.class);
    }

    @Test
    void activate_fromDraft_shouldBecomeActive() {
        Storefront storefront = draft();

        storefront.activate(now.plusSeconds(10));

        assertThat(storefront.getStatus()).isEqualTo(StorefrontStatus.ACTIVE);
        assertThat(storefront.getEvents().getLast()).isInstanceOf(StorefrontStatusChangedEvent.class);
    }

    @Test
    void activate_fromSuspended_shouldRejectTransition() {
        Storefront storefront = draft();
        storefront.activate(now);
        storefront.suspend(new LifecycleReason("Maintenance"), now);

        assertThatThrownBy(() -> storefront.activate(now))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void closed_isTerminal() {
        Storefront storefront = draft();
        storefront.close(new LifecycleReason("No longer needed"), now);

        assertThat(storefront.getStatus()).isEqualTo(StorefrontStatus.CLOSED);
        assertThatThrownBy(() -> storefront.activate(now))
                .isInstanceOf(MerchantDomainException.class);
        assertThatThrownBy(() -> storefront.rename(
                new StorefrontName("Other"),
                new StorefrontSlug("other-shop"),
                now
        )).isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void suspendThenReactivateThenClose_shouldFollowLifecycle() {
        Storefront storefront = draft();
        storefront.activate(now);
        storefront.suspend(new LifecycleReason("Policy review"), now);
        storefront.reactivate(now);
        storefront.close(new LifecycleReason("Requested closure"), now);

        assertThat(storefront.getStatus()).isEqualTo(StorefrontStatus.CLOSED);
        assertThatThrownBy(() -> storefront.reactivate(now))
                .isInstanceOf(MerchantDomainException.class);
    }

    @Test
    void rename_onActiveStorefront_shouldUpdateNameAndSlug() {
        Storefront storefront = draft();
        storefront.activate(now);

        storefront.rename(new StorefrontName("New Name"), new StorefrontSlug("new-name"), now);

        assertThat(storefront.getName().value()).isEqualTo("New Name");
        assertThat(storefront.getSlug().value()).isEqualTo("new-name");
    }

    private Storefront draft() {
        return Storefront.createDraft(
                new CommonId("storefront-1"),
                new CommonId("merchant-1"),
                new StorefrontName("Main Shop"),
                new StorefrontSlug("main-shop"),
                now
        );
    }
}

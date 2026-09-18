package com.saleschannel.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.domain.event.SalesChannelCreatedEvent;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalesChannelTest {
    private final Instant now = Instant.parse("2026-09-17T00:00:00Z");

    @Test
    void createWebsite_withMerchant_shouldEnableSellerOwnedChannel() {
        SalesChannel channel = SalesChannel.createWebsite(
                new CommonId("ch-1"),
                new CommonId("merchant-1"),
                "Website",
                now
        );

        assertThat(channel.getType()).isEqualTo(ChannelType.WEBSITE);
        assertThat(channel.getOwner()).isEqualTo(ChannelOwner.SELLER);
        assertThat(channel.getMerchantId().getValue()).isEqualTo("merchant-1");
        assertThat(channel.getStatus()).isEqualTo(ChannelStatus.ENABLED);
        assertThat(channel.getEvents().getFirst()).isInstanceOf(SalesChannelCreatedEvent.class);
    }

    @Test
    void createWebsite_withoutMerchant_shouldReject() {
        assertThatThrownBy(() -> SalesChannel.createWebsite(new CommonId("ch-1"), null, "Website", now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createMarketplace_shouldBePlatformOwnedWithoutMerchant() {
        SalesChannel channel = SalesChannel.createMarketplace(new CommonId("ch-mkt"), "Marketplace", now);

        assertThat(channel.getType()).isEqualTo(ChannelType.MARKETPLACE);
        assertThat(channel.getOwner()).isEqualTo(ChannelOwner.PLATFORM);
        assertThat(channel.getMerchantId()).isNull();
        assertThat(channel.getStatus()).isEqualTo(ChannelStatus.ENABLED);
    }

    @Test
    void disableThenEnable_shouldEmitLifecycleEvents() {
        SalesChannel channel = SalesChannel.createWebsite(
                new CommonId("ch-1"),
                new CommonId("merchant-1"),
                "Website",
                now
        );
        channel.pullEvents();

        channel.disable(now.plusSeconds(1));
        assertThat(channel.getStatus()).isEqualTo(ChannelStatus.DISABLED);
        assertThat(channel.getEvents().getLast()).isInstanceOf(SalesChannelDisabledEvent.class);

        channel.enable(now.plusSeconds(2));
        assertThat(channel.getStatus()).isEqualTo(ChannelStatus.ENABLED);
        assertThat(channel.getEvents().getLast()).isInstanceOf(SalesChannelEnabledEvent.class);
    }

    @Test
    void requireEnabled_whenDisabled_shouldReject() {
        SalesChannel channel = SalesChannel.createWebsite(
                new CommonId("ch-1"),
                new CommonId("merchant-1"),
                "Website",
                now
        );
        channel.disable(now.plusSeconds(1));

        assertThatThrownBy(channel::requireEnabled)
                .isInstanceOf(SalesChannelDomainException.class);
    }

    @Test
    void requireOwnedBy_sellerChannel_shouldRejectOtherMerchant() {
        SalesChannel channel = SalesChannel.createWebsite(
                new CommonId("ch-1"),
                new CommonId("merchant-1"),
                "Website",
                now
        );

        assertThatThrownBy(() -> channel.requireOwnedBy(new CommonId("merchant-2")))
                .isInstanceOf(SalesChannelDomainException.class);
    }

    @Test
    void requireOwnedBy_marketplace_shouldAllowAnyMerchant() {
        SalesChannel channel = SalesChannel.createMarketplace(new CommonId("ch-mkt"), "Marketplace", now);

        channel.requireOwnedBy(new CommonId("merchant-1"));
    }
}

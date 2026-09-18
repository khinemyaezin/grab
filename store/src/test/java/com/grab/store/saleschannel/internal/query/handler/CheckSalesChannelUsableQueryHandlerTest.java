package com.grab.store.saleschannel.internal.query.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.saleschannel.internal.query.CheckSalesChannelUsableQuery;
import com.grab.store.saleschannel.internal.query.CheckSalesChannelUsableResult;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.repository.SalesChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckSalesChannelUsableQueryHandlerTest {

    @Mock
    private SalesChannelRepository salesChannelRepository;

    private CheckSalesChannelUsableQueryHandler handler;

    @BeforeEach
    void setUp() {
        IdGenerator ids = new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("new");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
        handler = new CheckSalesChannelUsableQueryHandler(salesChannelRepository, ids);
    }

    @Test
    void handle_whenWebsiteOwnedAndEnabled_isUsable() {
        SalesChannel channel = SalesChannel.createWebsite(
                new CommonId("channel-1"),
                new CommonId("merchant-1"),
                "Store",
                Instant.parse("2026-09-18T00:00:00Z")
        );
        when(salesChannelRepository.findById(new CommonId("channel-1"))).thenReturn(Optional.of(channel));

        CheckSalesChannelUsableResult result = handler.handle(
                new CheckSalesChannelUsableQuery("channel-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.enabled()).isTrue();
        assertThat(result.ownedByMerchant()).isTrue();
        assertThat(result.usable()).isTrue();
    }

    @Test
    void handle_whenDisabled_isNotUsable() {
        SalesChannel channel = SalesChannel.createWebsite(
                new CommonId("channel-1"),
                new CommonId("merchant-1"),
                "Store",
                Instant.parse("2026-09-18T00:00:00Z")
        );
        channel.disable(Instant.parse("2026-09-18T01:00:00Z"));
        when(salesChannelRepository.findById(new CommonId("channel-1"))).thenReturn(Optional.of(channel));

        CheckSalesChannelUsableResult result = handler.handle(
                new CheckSalesChannelUsableQuery("channel-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.enabled()).isFalse();
        assertThat(result.ownedByMerchant()).isTrue();
        assertThat(result.usable()).isFalse();
    }

    @Test
    void handle_whenOwnedByAnotherMerchant_isNotUsable() {
        SalesChannel channel = SalesChannel.createWebsite(
                new CommonId("channel-1"),
                new CommonId("merchant-other"),
                "Store",
                Instant.parse("2026-09-18T00:00:00Z")
        );
        when(salesChannelRepository.findById(new CommonId("channel-1"))).thenReturn(Optional.of(channel));

        CheckSalesChannelUsableResult result = handler.handle(
                new CheckSalesChannelUsableQuery("channel-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.ownedByMerchant()).isFalse();
        assertThat(result.usable()).isFalse();
    }

    @Test
    void handle_whenMarketplace_countsAsOwned() {
        SalesChannel channel = SalesChannel.createMarketplace(
                new CommonId("market-1"),
                "Marketplace",
                Instant.parse("2026-09-18T00:00:00Z")
        );
        when(salesChannelRepository.findById(new CommonId("market-1"))).thenReturn(Optional.of(channel));

        CheckSalesChannelUsableResult result = handler.handle(
                new CheckSalesChannelUsableQuery("market-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.enabled()).isTrue();
        assertThat(result.ownedByMerchant()).isTrue();
        assertThat(result.usable()).isTrue();
    }

    @Test
    void handle_whenMissing_isNotUsable() {
        when(salesChannelRepository.findById(new CommonId("missing"))).thenReturn(Optional.empty());

        CheckSalesChannelUsableResult result = handler.handle(
                new CheckSalesChannelUsableQuery("missing", "merchant-1")
        );

        assertThat(result).isEqualTo(CheckSalesChannelUsableResult.missing());
        assertThat(result.usable()).isFalse();
    }
}

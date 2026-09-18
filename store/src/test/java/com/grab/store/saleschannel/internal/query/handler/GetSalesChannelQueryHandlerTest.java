package com.grab.store.saleschannel.internal.query.handler;

import com.grab.store.saleschannel.internal.exception.SalesChannelServiceException;
import com.grab.store.saleschannel.internal.query.GetSalesChannelQuery;
import com.grab.store.saleschannel.internal.query.SalesChannelResult;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.infrastructure.repository.jpa.SalesChannelQueryRepository;
import com.saleschannel.infrastructure.view.SalesChannelView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSalesChannelQueryHandlerTest {

    @Mock
    private SalesChannelQueryRepository salesChannelQueryRepository;

    private GetSalesChannelQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetSalesChannelQueryHandler(salesChannelQueryRepository);
    }

    @Test
    void handle_returnsOwnWebsite() {
        when(salesChannelQueryRepository.findById("web-1")).thenReturn(Optional.of(website("merchant-1")));

        SalesChannelResult result = handler.handle(new GetSalesChannelQuery("web-1", "merchant-1"));

        assertThat(result.salesChannelId()).isEqualTo("web-1");
        assertThat(result.type()).isEqualTo("WEBSITE");
    }

    @Test
    void handle_hidesOtherMerchantWebsite() {
        when(salesChannelQueryRepository.findById("web-1")).thenReturn(Optional.of(website("merchant-other")));

        assertThatThrownBy(() -> handler.handle(new GetSalesChannelQuery("web-1", "merchant-1")))
                .isInstanceOf(SalesChannelServiceException.class);
    }

    @Test
    void handle_returnsMarketplaceForAnyMerchant() {
        when(salesChannelQueryRepository.findById("market-1")).thenReturn(Optional.of(marketplace()));

        SalesChannelResult result = handler.handle(new GetSalesChannelQuery("market-1", "merchant-1"));

        assertThat(result.salesChannelId()).isEqualTo("market-1");
        assertThat(result.owner()).isEqualTo("PLATFORM");
    }

    private SalesChannelView website(String merchantId) {
        Instant now = Instant.parse("2026-09-18T00:00:00Z");
        return new SalesChannelView(
                "web-1",
                "Store",
                ChannelType.WEBSITE,
                ChannelOwner.SELLER,
                merchantId,
                ChannelStatus.ENABLED,
                now,
                now,
                0
        );
    }

    private SalesChannelView marketplace() {
        Instant now = Instant.parse("2026-09-18T00:00:00Z");
        return new SalesChannelView(
                "market-1",
                "Marketplace",
                ChannelType.MARKETPLACE,
                ChannelOwner.PLATFORM,
                null,
                ChannelStatus.ENABLED,
                now,
                now,
                0
        );
    }
}

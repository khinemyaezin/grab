package com.saleschannel.application.service;

import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;
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
class GetSalesChannelServiceTest {

    @Mock
    private SalesChannelQueryPort salesChannelQueryPort;

    private GetSalesChannelService service;

    @BeforeEach
    void setUp() {
        service = new GetSalesChannelService(salesChannelQueryPort);
    }

    @Test
    void execute_returnsOwnWebsite() {
        when(salesChannelQueryPort.findById("web-1")).thenReturn(Optional.of(website("merchant-1")));

        SalesChannelResult result = service.execute(new GetSalesChannelQuery("web-1", "merchant-1"));

        assertThat(result.salesChannelId()).isEqualTo("web-1");
        assertThat(result.type()).isEqualTo("WEBSITE");
    }

    @Test
    void execute_hidesOtherMerchantWebsite() {
        when(salesChannelQueryPort.findById("web-1")).thenReturn(Optional.of(website("merchant-other")));

        assertThatThrownBy(() -> service.execute(new GetSalesChannelQuery("web-1", "merchant-1")))
                .isInstanceOf(SalesChannelServiceException.class);
    }

    @Test
    void execute_returnsMarketplaceForAnyMerchant() {
        when(salesChannelQueryPort.findById("market-1")).thenReturn(Optional.of(marketplace()));

        SalesChannelResult result = service.execute(new GetSalesChannelQuery("market-1", "merchant-1"));

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

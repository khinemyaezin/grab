package com.saleschannel.application.service;

import com.saleschannel.application.model.read.CheckSalesChannelUsableQuery;
import com.saleschannel.application.model.read.CheckSalesChannelUsableResult;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckSalesChannelUsableServiceTest {

    @Mock
    private SalesChannelQueryPort salesChannelQueryPort;

    private CheckSalesChannelUsableService service;

    @BeforeEach
    void setUp() {
        service = new CheckSalesChannelUsableService(salesChannelQueryPort);
    }

    @Test
    void execute_whenWebsiteOwnedAndEnabled_isUsable() {
        when(salesChannelQueryPort.findById("channel-1")).thenReturn(Optional.of(view(
                "channel-1", ChannelType.WEBSITE, ChannelOwner.SELLER, "merchant-1", ChannelStatus.ENABLED
        )));

        CheckSalesChannelUsableResult result = service.execute(
                new CheckSalesChannelUsableQuery("channel-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.enabled()).isTrue();
        assertThat(result.ownedByMerchant()).isTrue();
        assertThat(result.usable()).isTrue();
    }

    @Test
    void execute_whenDisabled_isNotUsable() {
        when(salesChannelQueryPort.findById("channel-1")).thenReturn(Optional.of(view(
                "channel-1", ChannelType.WEBSITE, ChannelOwner.SELLER, "merchant-1", ChannelStatus.DISABLED
        )));

        CheckSalesChannelUsableResult result = service.execute(
                new CheckSalesChannelUsableQuery("channel-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.enabled()).isFalse();
        assertThat(result.ownedByMerchant()).isTrue();
        assertThat(result.usable()).isFalse();
    }

    @Test
    void execute_whenOwnedByAnotherMerchant_isNotUsable() {
        when(salesChannelQueryPort.findById("channel-1")).thenReturn(Optional.of(view(
                "channel-1", ChannelType.WEBSITE, ChannelOwner.SELLER, "merchant-other", ChannelStatus.ENABLED
        )));

        CheckSalesChannelUsableResult result = service.execute(
                new CheckSalesChannelUsableQuery("channel-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.ownedByMerchant()).isFalse();
        assertThat(result.usable()).isFalse();
    }

    @Test
    void execute_whenMarketplace_countsAsOwned() {
        when(salesChannelQueryPort.findById("market-1")).thenReturn(Optional.of(view(
                "market-1", ChannelType.MARKETPLACE, ChannelOwner.PLATFORM, null, ChannelStatus.ENABLED
        )));

        CheckSalesChannelUsableResult result = service.execute(
                new CheckSalesChannelUsableQuery("market-1", "merchant-1")
        );

        assertThat(result.found()).isTrue();
        assertThat(result.enabled()).isTrue();
        assertThat(result.ownedByMerchant()).isTrue();
        assertThat(result.usable()).isTrue();
    }

    @Test
    void execute_whenMissing_isNotUsable() {
        when(salesChannelQueryPort.findById("missing")).thenReturn(Optional.empty());

        CheckSalesChannelUsableResult result = service.execute(
                new CheckSalesChannelUsableQuery("missing", "merchant-1")
        );

        assertThat(result).isEqualTo(CheckSalesChannelUsableResult.missing());
        assertThat(result.usable()).isFalse();
    }

    private static SalesChannelView view(
            String id,
            ChannelType type,
            ChannelOwner owner,
            String merchantId,
            ChannelStatus status
    ) {
        return new SalesChannelView(
                id,
                "name",
                type,
                owner,
                merchantId,
                status,
                Instant.parse("2026-09-18T00:00:00Z"),
                Instant.parse("2026-09-18T00:00:00Z"),
                0L
        );
    }
}

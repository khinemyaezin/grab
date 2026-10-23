package com.saleschannel.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.saleschannel.application.model.write.ProvisionMerchantWebsiteChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import com.saleschannel.application.port.outbound.PrimaryMarketRegionPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProvisionMerchantWebsiteChannelServiceTest {
    private final SalesChannelRepository salesChannels = mock(SalesChannelRepository.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final PrimaryMarketRegionPort primaryMarketRegionPort = countryCode -> Optional.of(new CommonId("region-1"));
    private final ProvisionMerchantWebsiteChannelService service =
            new ProvisionMerchantWebsiteChannelService(salesChannels, idGenerator, primaryMarketRegionPort);

    @Test
    void execute_whenWebsiteExists_preservesExistingChannel() {
        CommonId merchantId = new CommonId("merchant-1");
        SalesChannel existing = SalesChannel.createWebsite(
                new CommonId("website-1"),
                merchantId,
                "Existing name",
                Instant.parse("2026-10-01T00:00:00Z")
        );
        existing.disable(Instant.parse("2026-10-02T00:00:00Z"));
        when(salesChannels.findWebsiteByMerchantId(merchantId)).thenReturn(Optional.of(existing));

        ProvisionSalesChannelResult result = service.execute(
                new ProvisionMerchantWebsiteChannelCommand(merchantId, "US")
        );

        assertThat(result.salesChannelId()).isEqualTo(existing.getId());
        assertThat(result.created()).isFalse();
        assertThat(existing.getStatus()).isEqualTo(ChannelStatus.DISABLED);
        verify(salesChannels, never()).save(existing);
        verify(idGenerator, never()).generateId();
    }

    @Test
    void execute_whenWebsiteMissing_createsSellerOwnedWebsite() {
        CommonId merchantId = new CommonId("merchant-1");
        CommonId channelId = new CommonId("website-1");
        when(salesChannels.findWebsiteByMerchantId(merchantId)).thenReturn(Optional.empty());
        when(idGenerator.generateId()).thenReturn(channelId);
        when(salesChannels.save(any(SalesChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProvisionSalesChannelResult result = service.execute(
                new ProvisionMerchantWebsiteChannelCommand(merchantId, "US")
        );

        assertThat(result.salesChannelId()).isEqualTo(channelId);
        assertThat(result.created()).isTrue();
        verify(salesChannels).save(org.mockito.ArgumentMatchers.argThat(channel ->
                channel.getName().equals("Website")
                        && channel.getType().name().equals("WEBSITE")
                        && channel.getOwner().name().equals("SELLER")
                        && channel.getMerchantId().equals(merchantId)
                        && channel.getStatus() == ChannelStatus.ENABLED));
    }
}

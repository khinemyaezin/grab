package com.saleschannel.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.saleschannel.application.model.write.EnsureMarketplaceChannelCommand;
import com.saleschannel.application.model.write.ProvisionSalesChannelResult;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnsureMarketplaceChannelServiceTest {
    private final SalesChannelRepository salesChannels = mock(SalesChannelRepository.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final EnsureMarketplaceChannelService service = new EnsureMarketplaceChannelService(salesChannels, idGenerator);

    @Test
    void execute_whenMarketplaceExists_preservesExistingChannel() {
        SalesChannel existing = SalesChannel.createMarketplace(
                new CommonId("marketplace-1"),
                "Existing name",
                Instant.parse("2026-10-01T00:00:00Z")
        );
        existing.disable(Instant.parse("2026-10-02T00:00:00Z"));
        when(salesChannels.findMarketplace()).thenReturn(Optional.of(existing));

        ProvisionSalesChannelResult result = service.execute(new EnsureMarketplaceChannelCommand());

        assertThat(result.salesChannelId()).isEqualTo(existing.getId());
        assertThat(result.created()).isFalse();
        assertThat(existing.getStatus()).isEqualTo(ChannelStatus.DISABLED);
        verify(salesChannels, never()).save(existing);
        verify(idGenerator, never()).generateId();
    }

    @Test
    void execute_whenMarketplaceMissing_createsPlatformMarketplace() {
        CommonId channelId = new CommonId("marketplace-1");
        when(salesChannels.findMarketplace()).thenReturn(Optional.empty());
        when(idGenerator.generateId()).thenReturn(channelId);
        when(salesChannels.save(any(SalesChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProvisionSalesChannelResult result = service.execute(new EnsureMarketplaceChannelCommand());

        assertThat(result.salesChannelId()).isEqualTo(channelId);
        assertThat(result.created()).isTrue();
        verify(salesChannels).save(org.mockito.ArgumentMatchers.argThat(channel ->
                channel.getName().equals("Default Sale Channel")
                        && channel.getType().name().equals("MARKETPLACE")
                        && channel.getOwner().name().equals("PLATFORM")
                        && channel.getMerchantId() == null
                        && channel.getStatus() == ChannelStatus.ENABLED));
    }
}

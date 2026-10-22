package com.saleschannel.application.service;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.Id;
import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.write.ApplySalesChannelRegionBindingsCommand;
import com.saleschannel.application.port.outbound.RegionStatusPort;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplySalesChannelRegionBindingsServiceTest {
    private final InMemorySalesChannels salesChannels = new InMemorySalesChannels();
    private final Set<Id> activeRegions = new HashSet<>();
    private final RegionStatusPort regions = activeRegions::contains;
    private final ApplySalesChannelRegionBindingsService service =
            new ApplySalesChannelRegionBindingsService(salesChannels, regions);

    @Test
    void execute_withValidBindings_shouldApplyAndSaveChannel() {
        CommonId channelId = new CommonId("channel-1");
        CommonId regionId = new CommonId("region-1");
        SalesChannel channel = channel(channelId);
        salesChannels.channels.put(channelId, channel);
        activeRegions.add(regionId);
        ApplySalesChannelRegionBindingsCommand command = new ApplySalesChannelRegionBindingsCommand(List.of(
                new ApplySalesChannelRegionBindingsCommand.Binding(channelId, regionId, true, true)
        ));

        Integer result = service.execute(command);

        assertThat(result).isEqualTo(1);
        assertThat(channel.servesRegion(regionId)).isTrue();
        assertThat(channel.getDefaultRegion()).isPresent();
        assertThat(salesChannels.saveCount).isEqualTo(1);
    }

    @Test
    void execute_withInactiveRegion_shouldFailBeforeSavingAnyChannel() {
        CommonId channelId = new CommonId("channel-1");
        CommonId regionId = new CommonId("region-1");
        SalesChannel channel = channel(channelId);
        salesChannels.channels.put(channelId, channel);
        ApplySalesChannelRegionBindingsCommand command = new ApplySalesChannelRegionBindingsCommand(List.of(
                new ApplySalesChannelRegionBindingsCommand.Binding(channelId, regionId, true, false)
        ));

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(SalesChannelServiceException.class);
        assertThat(salesChannels.saveCount).isZero();
    }

    @Test
    void execute_withTwoDefaultsForOneChannel_shouldRejectWholeCommand() {
        CommonId channelId = new CommonId("channel-1");
        CommonId firstRegionId = new CommonId("region-1");
        CommonId secondRegionId = new CommonId("region-2");
        salesChannels.channels.put(channelId, channel(channelId));
        activeRegions.add(firstRegionId);
        activeRegions.add(secondRegionId);
        ApplySalesChannelRegionBindingsCommand command = new ApplySalesChannelRegionBindingsCommand(List.of(
                new ApplySalesChannelRegionBindingsCommand.Binding(channelId, firstRegionId, true, true),
                new ApplySalesChannelRegionBindingsCommand.Binding(channelId, secondRegionId, true, true)
        ));

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(SalesChannelServiceException.class);
        assertThat(salesChannels.saveCount).isZero();
    }

    private SalesChannel channel(CommonId id) {
        Instant now = Instant.parse("2026-10-01T00:00:00Z");
        return new SalesChannel(
                id,
                "Website",
                ChannelType.WEBSITE,
                ChannelOwner.SELLER,
                new CommonId("merchant-1"),
                ChannelStatus.ENABLED,
                now,
                now,
                0,
                Set.of()
        );
    }

    private static class InMemorySalesChannels implements SalesChannelRepository {
        private final Map<Id, SalesChannel> channels = new HashMap<>();
        private int saveCount;

        @Override
        public Optional<SalesChannel> findById(Id id) {
            return Optional.ofNullable(channels.get(id));
        }

        @Override
        public Optional<SalesChannel> findWebsiteByMerchantId(Id merchantId) {
            return Optional.empty();
        }

        @Override
        public Optional<SalesChannel> findMarketplace() {
            return Optional.empty();
        }

        @Override
        public boolean existsWebsiteByMerchantId(Id merchantId) {
            return false;
        }

        @Override
        public boolean existsMarketplace() {
            return false;
        }

        @Override
        public SalesChannel save(SalesChannel channel) {
            saveCount++;
            channels.put(channel.getId(), channel);
            return channel;
        }
    }
}

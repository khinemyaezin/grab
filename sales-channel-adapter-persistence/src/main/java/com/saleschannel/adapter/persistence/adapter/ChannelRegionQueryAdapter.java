package com.saleschannel.adapter.persistence.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.adapter.persistence.repository.ChannelRegionJpaRepository;
import com.saleschannel.application.port.outbound.ChannelRegionLookupPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ChannelRegionQueryAdapter implements ChannelRegionLookupPort {
    private final ChannelRegionJpaRepository channelRegions;
    private final IdGenerator ids;
    private final PersistenceExecutor executor;

    @Override
    public boolean servesRegion(Id salesChannelId, Id regionId) {
        String channelValue = salesChannelId.getValue();
        String regionValue = regionId.getValue();
        return executor.query("ChannelRegion", () -> channelRegions
                .existsByIdSalesChannelIdAndIdRegionIdAndEnabledTrue(channelValue, regionValue));
    }

    @Override
    public Optional<Id> findDefaultRegion(Id salesChannelId) {
        String channelValue = salesChannelId.getValue();
        return executor.query("ChannelRegion", () -> channelRegions.findDefaultRegionId(channelValue)
                .map(ids::convertIdFrom));
    }

    @Override
    public List<Id> findEnabledRegions(Id salesChannelId) {
        String channelValue = salesChannelId.getValue();
        return executor.query("ChannelRegion", () -> channelRegions.findEnabledRegionIds(channelValue)
                .stream()
                .map(ids::convertIdFrom)
                .toList());
    }
}

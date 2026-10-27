package com.grab.app.channel.discovery.internal.adapter;

import com.grab.discovery.application.port.outbound.SalesChannelRegionPort;
import com.grab.framework.id.Id;
import com.grab.app.channel.saleschannel.port.ChannelRegionQueryPort;
import com.grab.app.channel.discovery.internal.adapter.mapper.SalesChannelRegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelRegionAdapter implements SalesChannelRegionPort {
    private final ChannelRegionQueryPort channelRegions;
    private final SalesChannelRegionMapper mapper;

    @Override
    public Optional<Id> findDefaultRegion(Id salesChannelId) {
        return mapper.toDefaultRegion(channelRegions.findDefaultRegion(salesChannelId));
    }

    @Override
    public boolean servesRegion(Id salesChannelId, Id regionId) {
        return channelRegions.servesRegion(salesChannelId, regionId);
    }
}

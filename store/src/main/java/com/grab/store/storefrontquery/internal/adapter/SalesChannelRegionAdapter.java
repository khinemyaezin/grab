package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.SalesChannelRegionPort;
import com.grab.framework.id.Id;
import com.grab.store.saleschannel.port.ChannelRegionQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelRegionAdapter implements SalesChannelRegionPort {
    private final ChannelRegionQueryPort channelRegions;

    @Override
    public Optional<Id> findDefaultRegion(Id salesChannelId) {
        return channelRegions.findDefaultRegion(salesChannelId);
    }

    @Override
    public boolean servesRegion(Id salesChannelId, Id regionId) {
        return channelRegions.servesRegion(salesChannelId, regionId);
    }
}

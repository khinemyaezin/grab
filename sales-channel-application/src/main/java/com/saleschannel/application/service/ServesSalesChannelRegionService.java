package com.saleschannel.application.service;

import com.grab.framework.id.Id;
import com.saleschannel.application.port.inbound.ServesSalesChannelRegionUseCase;
import com.saleschannel.application.port.outbound.ChannelRegionLookupPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServesSalesChannelRegionService implements ServesSalesChannelRegionUseCase {
    private final ChannelRegionLookupPort channelRegionLookupPort;

    @Override
    public boolean execute(Id salesChannelId, Id regionId) {
        return channelRegionLookupPort.servesRegion(salesChannelId, regionId);
    }
}

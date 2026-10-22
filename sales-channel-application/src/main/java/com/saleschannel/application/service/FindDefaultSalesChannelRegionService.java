package com.saleschannel.application.service;

import com.grab.framework.id.Id;
import com.saleschannel.application.port.inbound.FindDefaultSalesChannelRegionUseCase;
import com.saleschannel.application.port.outbound.ChannelRegionLookupPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindDefaultSalesChannelRegionService implements FindDefaultSalesChannelRegionUseCase {
    private final ChannelRegionLookupPort channelRegionLookupPort;

    @Override
    public Optional<Id> execute(Id salesChannelId) {
        return channelRegionLookupPort.findDefaultRegion(salesChannelId);
    }
}

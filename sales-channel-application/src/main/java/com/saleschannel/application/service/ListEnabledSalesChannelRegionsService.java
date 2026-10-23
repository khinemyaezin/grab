package com.saleschannel.application.service;

import com.grab.framework.id.Id;
import com.saleschannel.application.port.inbound.ListEnabledSalesChannelRegionsUseCase;
import com.saleschannel.application.port.outbound.ChannelRegionLookupPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListEnabledSalesChannelRegionsService implements ListEnabledSalesChannelRegionsUseCase {
    private final ChannelRegionLookupPort channelRegionLookupPort;

    @Override
    public List<Id> execute(Id salesChannelId) {
        return channelRegionLookupPort.findEnabledRegions(salesChannelId);
    }
}

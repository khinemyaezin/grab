package com.grab.app.channeldiscovery.saleschannel.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.app.channeldiscovery.saleschannel.port.ChannelRegionQueryPort;
import com.saleschannel.application.port.inbound.FindDefaultSalesChannelRegionUseCase;
import com.saleschannel.application.port.inbound.ListEnabledSalesChannelRegionsUseCase;
import com.saleschannel.application.port.inbound.ServesSalesChannelRegionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChannelRegionQueryPortAdapter implements ChannelRegionQueryPort {
    private final ServesSalesChannelRegionUseCase servesSalesChannelRegionUseCase;
    private final FindDefaultSalesChannelRegionUseCase findDefaultSalesChannelRegionUseCase;
    private final ListEnabledSalesChannelRegionsUseCase listEnabledSalesChannelRegionsUseCase;

    @Override
    @SalesChannelReadTransactional
    public boolean servesRegion(Id salesChannelId, Id regionId) {
        return servesSalesChannelRegionUseCase.execute(salesChannelId, regionId);
    }

    @Override
    @SalesChannelReadTransactional
    public Optional<Id> findDefaultRegion(Id salesChannelId) {
        return findDefaultSalesChannelRegionUseCase.execute(salesChannelId);
    }

    @Override
    @SalesChannelReadTransactional
    public List<Id> findEnabledRegions(Id salesChannelId) {
        return listEnabledSalesChannelRegionsUseCase.execute(salesChannelId);
    }
}

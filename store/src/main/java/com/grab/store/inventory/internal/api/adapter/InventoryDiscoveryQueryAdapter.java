package com.grab.store.inventory.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.store.inventory.internal.api.adapter.mapper.InventoryDiscoveryQueryMapper;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.port.InventoryDiscoveryQuery;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.port.inbound.GetDiscoveryInventoryFactsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryDiscoveryQueryAdapter implements InventoryDiscoveryQuery {
    private final GetDiscoveryInventoryFactsUseCase getDiscoveryInventoryFactsUseCase;
    private final InventoryDiscoveryQueryMapper mapper;

    @Override
    @InventoryReadTransactional
    public DiscoveryAvailability discoveryAvailability(Id merchantId, String sku, Id salesChannelId) {
        GetDiscoveryInventoryFactsQuery query = new GetDiscoveryInventoryFactsQuery(
                merchantId,
                sku,
                salesChannelId
        );
        return mapper.toAvailability(getDiscoveryInventoryFactsUseCase.execute(query));
    }
}

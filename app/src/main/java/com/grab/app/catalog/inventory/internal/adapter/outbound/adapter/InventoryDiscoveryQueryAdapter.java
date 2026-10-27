package com.grab.app.catalog.inventory.internal.adapter.outbound;

import com.grab.framework.id.Id;
import com.grab.app.catalog.inventory.internal.adapter.outbound.mapper.InventoryDiscoveryQueryMapper;
import com.grab.app.catalog.inventory.internal.config.InventoryReadTransactional;
import com.grab.app.catalog.inventory.port.InventoryDiscoveryQuery;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.model.read.ListSkusAtLocationQuery;
import com.inventory.application.port.inbound.GetDiscoveryInventoryFactsUseCase;
import com.inventory.application.port.inbound.ListSkusAtLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryDiscoveryQueryAdapter implements InventoryDiscoveryQuery {
    private final GetDiscoveryInventoryFactsUseCase getDiscoveryInventoryFactsUseCase;
    private final ListSkusAtLocationUseCase listSkusAtLocationUseCase;
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

    @Override
    @InventoryReadTransactional
    public List<String> skusAtLocation(String locationId) {
        ListSkusAtLocationQuery query = new ListSkusAtLocationQuery(locationId);
        return listSkusAtLocationUseCase.execute(query);
    }
}

package com.grab.app.channel.discovery.internal.adapter;

import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.framework.id.Id;
import com.grab.app.catalog.inventory.port.InventoryDiscoveryQuery;
import com.grab.app.channel.discovery.internal.adapter.mapper.InventoryDiscoveryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryDiscoveryAdapter implements InventoryPort {
    private final InventoryDiscoveryQuery inventory;
    private final InventoryDiscoveryMapper mapper;

    @Override
    public Availability available(String sku, Id merchantId, Id salesChannelId) {
        InventoryDiscoveryQuery.DiscoveryAvailability availability = inventory.discoveryAvailability(
                merchantId,
                sku,
                salesChannelId
        );
        return mapper.toAvailability(availability);
    }

    @Override
    public List<String> skusAtLocation(Id locationId) {
        return inventory.skusAtLocation(locationId.getValue());
    }
}

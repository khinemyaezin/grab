package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort.DiscoveryStockFactResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetDiscoveryInventoryFactsPortMapper {

    public GetDiscoveryInventoryFactsQuery toQuery(Id merchantId, String sku, Id salesChannelId) {
        return new GetDiscoveryInventoryFactsQuery(merchantId, sku, salesChannelId);
    }

    public abstract DiscoveryStockFactResult toResponse(GetDiscoveryInventoryFactsResult result);
}


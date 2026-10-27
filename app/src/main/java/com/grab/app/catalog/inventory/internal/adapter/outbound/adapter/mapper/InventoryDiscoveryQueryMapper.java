package com.grab.app.catalog.inventory.internal.adapter.outbound.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.CentralMapperConfig;
import com.grab.app.catalog.inventory.port.InventoryDiscoveryQuery.DiscoveryAvailability;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class InventoryDiscoveryQueryMapper {
    public abstract DiscoveryAvailability toAvailability(GetDiscoveryInventoryFactsResult result);
}

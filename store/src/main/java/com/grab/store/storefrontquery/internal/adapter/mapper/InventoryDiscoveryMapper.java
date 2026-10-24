package com.grab.store.storefrontquery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.port.InventoryDiscoveryQuery;
import com.grab.store.storefrontquery.internal.api.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class InventoryDiscoveryMapper {
    public abstract InventoryPort.Availability toAvailability(InventoryDiscoveryQuery.DiscoveryAvailability availability);
}

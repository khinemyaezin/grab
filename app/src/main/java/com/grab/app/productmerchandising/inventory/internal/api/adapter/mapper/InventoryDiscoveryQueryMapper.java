package com.grab.app.productmerchandising.inventory.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.productmerchandising.inventory.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.app.productmerchandising.inventory.port.InventoryDiscoveryQuery.DiscoveryAvailability;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class InventoryDiscoveryQueryMapper {
    public abstract DiscoveryAvailability toAvailability(GetDiscoveryInventoryFactsResult result);
}

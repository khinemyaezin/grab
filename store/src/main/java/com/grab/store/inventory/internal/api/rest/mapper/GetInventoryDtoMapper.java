package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.query.GetInventoryQuery;
import com.grab.store.inventory.internal.query.GetInventoryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventoryDtoMapper {

    public abstract GetInventoryQuery toQuery(String inventoryItemId);

    public abstract InventoryResponse toResponse(GetInventoryResult result);
}

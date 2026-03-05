package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryMovementsResponse;
import com.grab.store.inventory.internal.query.GetInventoryMovementsQuery;
import com.grab.store.inventory.internal.query.GetInventoryMovementsResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InventoryMovementsDtoMapper {

    public abstract GetInventoryMovementsQuery toQuery(String inventoryItemId);

    public abstract InventoryMovementsResponse toResponse(GetInventoryMovementsResult result);
}

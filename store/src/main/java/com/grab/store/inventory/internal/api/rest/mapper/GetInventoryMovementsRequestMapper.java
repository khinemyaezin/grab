package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.StockMovementResponse;
import com.inventory.application.model.read.GetInventoryMovementsQuery;
import com.inventory.application.model.read.GetInventoryMovementsResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventoryMovementsRequestMapper {

    public abstract GetInventoryMovementsQuery toQuery(String inventoryItemId, Pageable pageable);

    public abstract StockMovementResponse toResponse(GetInventoryMovementsResult result);
}

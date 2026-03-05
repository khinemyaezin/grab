package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationsResponse;
import com.grab.store.inventory.internal.query.GetInventoryReservationsQuery;
import com.grab.store.inventory.internal.query.GetInventoryReservationsResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InventoryReservationsDtoMapper {

    public abstract GetInventoryReservationsQuery toQuery(String inventoryItemId);

    public abstract InventoryReservationsResponse toResponse(GetInventoryReservationsResult result);
}

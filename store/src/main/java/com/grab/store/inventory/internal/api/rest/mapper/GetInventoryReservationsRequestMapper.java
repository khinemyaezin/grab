package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.query.GetInventoryReservationsQuery;
import com.grab.store.inventory.internal.query.GetInventoryReservationsResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventoryReservationsRequestMapper {

    public abstract GetInventoryReservationsQuery toQuery(String inventoryItemId, Pageable pageable);

    public abstract InventoryReservationResponse toResponse(GetInventoryReservationsResult result);
}

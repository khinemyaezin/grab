package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryMovementsModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryReservationsModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryMovementsResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationsResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryDtoMapper;
import com.grab.store.inventory.internal.api.rest.mapper.InventoryMovementsDtoMapper;
import com.grab.store.inventory.internal.api.rest.mapper.InventoryReservationsDtoMapper;
import com.grab.store.inventory.internal.query.GetInventoryMovementsQuery;
import com.grab.store.inventory.internal.query.GetInventoryMovementsResult;
import com.grab.store.inventory.internal.query.GetInventoryQuery;
import com.grab.store.inventory.internal.query.GetInventoryReservationsQuery;
import com.grab.store.inventory.internal.query.GetInventoryReservationsResult;
import com.grab.store.inventory.internal.query.GetInventoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryQueryService {

    private final QueryBus queryBus;
    private final GetInventoryDtoMapper getInventoryDtoMapper;
    private final InventoryMovementsDtoMapper inventoryMovementsDtoMapper;
    private final InventoryReservationsDtoMapper inventoryReservationsDtoMapper;
    private final InventoryModelAssembler inventoryModelAssembler;
    private final InventoryMovementsModelAssembler inventoryMovementsModelAssembler;
    private final InventoryReservationsModelAssembler inventoryReservationsModelAssembler;

    public EntityModel<InventoryResponse> getInventory(String inventoryItemId) {
        GetInventoryQuery query = getInventoryDtoMapper.toQuery(inventoryItemId);
        GetInventoryResult result = queryBus.dispatch(query);
        InventoryResponse response = getInventoryDtoMapper.toResponse(result);
        return inventoryModelAssembler.toModel(response);
    }

    public EntityModel<InventoryMovementsResponse> getMovements(String inventoryItemId) {
        GetInventoryMovementsQuery query = inventoryMovementsDtoMapper.toQuery(inventoryItemId);
        GetInventoryMovementsResult result = queryBus.dispatch(query);
        InventoryMovementsResponse response = inventoryMovementsDtoMapper.toResponse(result);
        return inventoryMovementsModelAssembler.toModel(response);
    }

    public EntityModel<InventoryReservationsResponse> getReservations(String inventoryItemId) {
        GetInventoryReservationsQuery query = inventoryReservationsDtoMapper.toQuery(inventoryItemId);
        GetInventoryReservationsResult result = queryBus.dispatch(query);
        InventoryReservationsResponse response = inventoryReservationsDtoMapper.toResponse(result);
        return inventoryReservationsModelAssembler.toModel(response);
    }
}

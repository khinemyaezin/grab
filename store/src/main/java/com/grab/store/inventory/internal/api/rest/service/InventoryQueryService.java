package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.StockMovementResponse;
import com.grab.store.inventory.internal.query.GetInventoryMovementsResult;
import com.grab.store.inventory.internal.query.GetInventoryReservationsResult;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryMovementsRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryReservationsRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryQueryService {

    private final QueryBus queryBus;
    private final GetInventoryRequestMapper getInventoryRequestMapper;
    private final GetInventoryMovementsRequestMapper getInventoryMovementsRequestMapper;
    private final GetInventoryReservationsRequestMapper getInventoryReservationsRequestMapper;

    public InventoryResponse getInventory(String inventoryItemId) {
        var query = getInventoryRequestMapper.toQuery(inventoryItemId);
        var result = queryBus.dispatch(query);
        return getInventoryRequestMapper.toResponse(result);
    }

    public Page<StockMovementResponse> getMovements(String inventoryItemId, Pageable pageable) {
        var query = getInventoryMovementsRequestMapper.toQuery(inventoryItemId, pageable);
        Page<GetInventoryMovementsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(getInventoryMovementsRequestMapper::toResponse);
    }

    public Page<InventoryReservationResponse> getReservations(String inventoryItemId, Pageable pageable) {
        var query = getInventoryReservationsRequestMapper.toQuery(inventoryItemId, pageable);
        Page<GetInventoryReservationsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(getInventoryReservationsRequestMapper::toResponse);
    }
}

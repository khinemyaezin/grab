package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.StockMovementResponse;
import com.grab.store.inventory.internal.query.GetInventoryLocationIdQuery;
import com.grab.store.inventory.internal.query.GetInventoryMovementsResult;
import com.grab.store.inventory.internal.query.GetInventoryReservationsResult;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryMovementsRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryReservationsRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.SearchInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchInventoryRequest;
import com.grab.store.inventory.internal.query.SearchInventoryResult;
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
    private final SearchInventoryRequestMapper searchInventoryRequestMapper;
    private final IdGenerator idGenerator;

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

    public Page<InventoryResponse> searchInventoryItems(String merchantId, SearchInventoryRequest request, Pageable pageable) {
        var query = searchInventoryRequestMapper.toQuery(merchantId, request, pageable);
        Page<SearchInventoryResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(searchInventoryRequestMapper::toResponse);
    }

    public String getLocationId(String inventoryItemId) {
        var query = new GetInventoryLocationIdQuery(idGenerator.convertIdFrom(inventoryItemId));
        return queryBus.dispatch(query);
    }
}

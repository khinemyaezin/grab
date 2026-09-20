package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.api.rest.dto.request.CheckInventoryExistenceRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocationAvailabilityResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.CheckInventoryExistenceResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventorySummaryResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.ReorderSuggestionResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.StockMovementResponse;
import com.inventory.application.model.read.GetInventoryLocationIdQuery;
import com.inventory.application.model.read.GetInventoryMovementsResult;
import com.inventory.application.model.read.GetInventoryReservationsResult;
import com.grab.store.inventory.internal.api.rest.mapper.CheckInventoryExistenceRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryMovementsRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventoryReservationsRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetInventorySummaryRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.SearchInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchInventoryRequest;
import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;
import com.inventory.application.model.read.GetAllocationAvailabilityQuery;
import com.inventory.application.model.read.GetAllocationAvailabilityResult;
import com.inventory.application.model.read.GetInventoryQuery;
import com.inventory.application.model.read.GetInventoryResult;
import com.inventory.application.model.read.GetInventoryMovementsQuery;
import com.inventory.application.model.read.GetInventoryReservationsQuery;
import com.inventory.application.model.read.GetInventorySummaryQuery;
import com.inventory.application.model.read.GetInventorySummaryResult;
import com.inventory.application.model.read.GetReorderSuggestionResult;
import com.inventory.application.model.read.GetReorderSuggestionsQuery;
import com.inventory.application.model.read.SearchInventoryQuery;
import com.inventory.application.model.read.SearchInventoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryQueryService {

    private final QueryBus queryBus;
    private final GetInventoryRequestMapper getInventoryRequestMapper;
    private final GetInventoryMovementsRequestMapper getInventoryMovementsRequestMapper;
    private final GetInventoryReservationsRequestMapper getInventoryReservationsRequestMapper;
    private final SearchInventoryRequestMapper searchInventoryRequestMapper;
    private final CheckInventoryExistenceRequestMapper checkInventoryExistenceRequestMapper;
    private final GetInventorySummaryRequestMapper getInventorySummaryRequestMapper;
    private final IdGenerator idGenerator;

    public InventoryResponse getInventory(String inventoryItemId) {
        GetInventoryQuery query = getInventoryRequestMapper.toQuery(inventoryItemId);
        GetInventoryResult result = queryBus.dispatch(query);
        return getInventoryRequestMapper.toResponse(result);
    }

    public Page<StockMovementResponse> getMovements(String inventoryItemId, Pageable pageable) {
        GetInventoryMovementsQuery query = getInventoryMovementsRequestMapper.toQuery(inventoryItemId, pageable);
        Page<GetInventoryMovementsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(getInventoryMovementsRequestMapper::toResponse);
    }

    public Page<InventoryReservationResponse> getReservations(String inventoryItemId, Pageable pageable) {
        GetInventoryReservationsQuery query = getInventoryReservationsRequestMapper.toQuery(inventoryItemId, pageable);
        Page<GetInventoryReservationsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(getInventoryReservationsRequestMapper::toResponse);
    }

    public Page<InventoryResponse> searchInventoryItems(String merchantId, SearchInventoryRequest request, Pageable pageable) {
        SearchInventoryQuery query = searchInventoryRequestMapper.toQuery(merchantId, request, pageable);
        Page<SearchInventoryResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(searchInventoryRequestMapper::toResponse);
    }

    public CheckInventoryExistenceResponse checkExistence(String merchantId, CheckInventoryExistenceRequest request) {
        CheckInventoryExistenceQuery query = checkInventoryExistenceRequestMapper.toQuery(merchantId, request);
        CheckInventoryExistenceResult result = queryBus.dispatch(query);
        return checkInventoryExistenceRequestMapper.toResponse(result);
    }

    public String getLocationId(String inventoryItemId) {
        GetInventoryLocationIdQuery query = new GetInventoryLocationIdQuery(idGenerator.convertIdFrom(inventoryItemId));
        return queryBus.dispatch(query);
    }

    public AllocationAvailabilityResponse getAllocationAvailability(String sku, Integer quantity, String salesChannelId) {
        GetAllocationAvailabilityResult result = queryBus.dispatch(new GetAllocationAvailabilityQuery(sku, quantity, salesChannelId));
        return new AllocationAvailabilityResponse(
                result.sku(),
                result.availableQuantity(),
                result.canAllocate(),
                result.requestedQuantity()
        );
    }

    public InventorySummaryResponse getInventorySummary(String merchantId, String locationId) {
        GetInventorySummaryQuery query = getInventorySummaryRequestMapper.toQuery(merchantId, locationId);
        GetInventorySummaryResult result = queryBus.dispatch(query);
        return getInventorySummaryRequestMapper.toResponse(result);
    }

    public List<ReorderSuggestionResponse> getReorderSuggestions(String merchantId, String locationId, String sku) {
        List<GetReorderSuggestionResult> results = queryBus.dispatch(new GetReorderSuggestionsQuery(
                idGenerator.convertIdFrom(merchantId),
                locationId == null || locationId.isBlank() ? null : idGenerator.convertIdFrom(locationId),
                sku
        ));
        return results.stream()
                .map(result -> new ReorderSuggestionResponse(
                        result.inventoryItemId(),
                        result.sku(),
                        result.productName(),
                        result.productVariantId(),
                        result.locationId(),
                        result.currentAvailable(),
                        result.reorderPoint(),
                        result.suggestedQuantity(),
                        result.priority()
                ))
                .toList();
    }
}

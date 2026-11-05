package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.AllocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateInventoryItemRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.DeallocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StockMovementResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Seller Central Inventory tracking, allocation, and restock.
 */
public interface SellerInventoryResource {

    ResponseEntity<EntityModel<InventoryItemResponse>> createItem(CreateInventoryItemRequest request, String merchantId, String actorId);

    ResponseEntity<EntityModel<InventoryItemResponse>> getItem(String itemId);

    ResponseEntity<EntityModel<StockMovementResponse>> getLatestMovement(String itemId);

    /**
     * Public REST Contract for Stock Reservations and Allocations.
     */
    interface Allocations {
        ResponseEntity<EntityModel<AllocationResponse>> allocateStock(AllocateStockRequest request, String actorId);
        ResponseEntity<Void> deallocateStock(DeallocateStockRequest request, String actorId);
    }

    /**
     * Public REST Contract for Reorder Suggestions and Restock Analytics.
     */
    interface ReorderSuggestions {
        ResponseEntity<CollectionModel<EntityModel<ReorderSuggestionResponse>>> listSuggestions(String merchantId, String locationId, String sku);
    }
}

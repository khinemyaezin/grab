package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

public record InventoryItemResponse(
        String id,
        String sku,
        String productName,
        String locationId,
        String locationCode,
        String locationName,
        int onHand,
        int reserved,
        int inTransit,
        int damaged,
        int available,
        String status,
        int safetyStock,
        int reorderPoint,
        int reorderQuantity,
        Integer maxStock
) {}

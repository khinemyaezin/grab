package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record VariantResponse(
        String id,
        String sku,
        String status,
        String matrixKey,
        boolean manageInventory
) {}

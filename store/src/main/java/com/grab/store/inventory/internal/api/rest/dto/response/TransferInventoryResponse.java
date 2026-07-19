package com.grab.store.inventory.internal.api.rest.dto.response;

public record TransferInventoryResponse(
        InventoryResponse source,
        InventoryResponse destination,
        String transferId
) {
}

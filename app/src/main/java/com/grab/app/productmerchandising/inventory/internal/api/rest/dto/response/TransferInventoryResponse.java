package com.grab.app.productmerchandising.inventory.internal.api.rest.dto.response;

public record TransferInventoryResponse(
        InventoryResponse source,
        InventoryResponse destination,
        String transferId
) {
}

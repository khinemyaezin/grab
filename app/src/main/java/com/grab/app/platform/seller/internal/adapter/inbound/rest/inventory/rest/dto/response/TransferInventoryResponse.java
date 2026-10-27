package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response;

public record TransferInventoryResponse(
        InventoryResponse source,
        InventoryResponse destination,
        String transferId
) {
}

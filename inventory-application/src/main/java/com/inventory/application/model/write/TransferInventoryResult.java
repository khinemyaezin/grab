package com.inventory.application.model.write;

public record TransferInventoryResult(
        InventoryItemResult source,
        InventoryItemResult destination,
        String transferId
) {
}

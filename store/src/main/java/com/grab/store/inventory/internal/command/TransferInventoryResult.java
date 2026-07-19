package com.grab.store.inventory.internal.command;

public record TransferInventoryResult(
        InventoryItemResult source,
        InventoryItemResult destination,
        String transferId
) {
}

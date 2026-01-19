package com.inventory.domain.service;

import com.grab.framework.id.Id;

public interface InventoryTransferService {

    record TransferResult(
            boolean success,
            String sku,
            Id sourceLocationId,
            Id destinationLocationId,
            int quantity,
            String transferId,
            String failureReason
    ) {
        public static TransferResult success(String sku, Id source, Id dest, int qty, String transferId) {
            return new TransferResult(true, sku, source, dest, qty, transferId, null);
        }

        public static TransferResult failure(String sku, Id source, Id dest, int qty, String reason) {
            return new TransferResult(false, sku, source, dest, qty, null, reason);
        }
    }

    TransferResult transfer(
            String sku,
            Id sourceLocationId,
            Id destinationLocationId,
            int quantity,
            String notes
    );

    TransferResult initiateTransfer(
            String sku,
            Id sourceLocationId,
            Id destinationLocationId,
            int quantity,
            String notes
    );

    TransferResult completeTransfer(String transferId, int actualQuantity);

    TransferResult cancelTransfer(String transferId, String reason);

    boolean canTransfer(String sku, Id sourceLocationId, int quantity);
}

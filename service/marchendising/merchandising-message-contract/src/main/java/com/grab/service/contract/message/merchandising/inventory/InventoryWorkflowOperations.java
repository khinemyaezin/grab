package com.grab.service.contract.message.merchandising.inventory;

import com.grab.service.contract.event.merchandising.inventory.InventoryWorkflowPayloads.InventoryBatch;
import com.grab.service.contract.event.merchandising.inventory.InventoryWorkflowPayloads.InventoryWritten;
import com.grab.service.contract.event.merchandising.inventory.InventoryWorkflowPayloads.StockPaths;
import com.grab.framework.workflow.WorkflowOperation;
import com.grab.framework.workflow.WorkflowUnit;

public final class InventoryWorkflowOperations {

    private InventoryWorkflowOperations() {
    }

    public static final WorkflowOperation<InventoryBatch, InventoryWritten> APPLY_OPERATIONS =
            WorkflowOperation.mutation("inventory.apply-operations", InventoryBatch.class, InventoryWritten.class);
    public static final WorkflowOperation<InventoryBatch, InventoryWritten> CREATE_ITEMS =
            WorkflowOperation.mutation("inventory.create-items", InventoryBatch.class, InventoryWritten.class);
    public static final WorkflowOperation<InventoryBatch, InventoryWritten> SYNC_ITEMS =
            WorkflowOperation.mutation("inventory.sync-product-items", InventoryBatch.class, InventoryWritten.class);
    public static final WorkflowOperation<StockPaths, WorkflowUnit> ASSERT_STOCK =
            WorkflowOperation.assertion("inventory.assert-stock-paths", StockPaths.class, WorkflowUnit.class);
}

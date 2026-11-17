package com.grab.service.contract.message.merchandising.pricing;

import com.grab.service.contract.event.merchandising.pricing.PricingWorkflowPayloads.PriceBatch;
import com.grab.service.contract.event.merchandising.pricing.PricingWorkflowPayloads.PricesWritten;
import com.grab.framework.workflow.WorkflowOperation;

public final class PricingWorkflowOperations {

    private PricingWorkflowOperations() {
    }

    public static final WorkflowOperation<PriceBatch, PricesWritten> SYNC_VARIANT =
            WorkflowOperation.mutation("pricing.sync-variant-prices", PriceBatch.class, PricesWritten.class);
    public static final WorkflowOperation<PriceBatch, PricesWritten> SYNC_PRODUCT =
            WorkflowOperation.mutation("pricing.sync-product-prices", PriceBatch.class, PricesWritten.class);
    public static final WorkflowOperation<PriceBatch, PricesWritten> CREATE_PRICES =
            WorkflowOperation.mutation("pricing.create-variant-price-sets", PriceBatch.class, PricesWritten.class);
}

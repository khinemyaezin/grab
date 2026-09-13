package com.grab.store.workflows.createsellableproduct;

import com.grab.store.workflows.internal.service.WorkflowTerminalDetails;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductContext;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductTerminalContextAdapter;
import com.grab.store.workflows.internal.workflows.createsellableproduct.CreateSellableProductWorkflowNames;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSellableProductTerminalContextAdapterTest {

    private final CreateSellableProductTerminalContextAdapter adapter =
            new CreateSellableProductTerminalContextAdapter();

    @Test
    void from_whenNoInventory_shouldNotMarkPartial() {
        CreateSellableProductContext context = CreateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                new CreateSellableProductContext.Product("Shirt", "cat-1", "NEW", "shirt", List.of()),
                List.of(),
                List.of(),
                List.of()
        );

        assertThat(adapter.workflowName()).isEqualTo(CreateSellableProductWorkflowNames.WORKFLOW_NAME);
        assertThat(adapter.from(context)).isEqualTo(new WorkflowTerminalDetails("actor-1", "merchant-1", false));
    }

    @Test
    void from_whenInventoryRemains_shouldMarkPartial() {
        CreateSellableProductContext context = CreateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                new CreateSellableProductContext.Product("Shirt", "cat-1", "NEW", "shirt", List.of()),
                List.of(),
                List.of(),
                List.of()
        ).withInventoryItem(new CreateSellableProductContext.InventoryItemRef("inv-1", "SKU-1", "loc-1"));

        assertThat(adapter.from(context).partiallyApplied()).isTrue();
    }
}

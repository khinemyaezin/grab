package com.grab.store.workflows.updatesellableproduct;

import com.grab.store.workflows.internal.service.WorkflowTerminalDetails;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductContext;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductTerminalContextAdapter;
import com.grab.store.workflows.internal.workflows.updatesellableproduct.UpdateSellableProductWorkflowNames;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductTerminalContextAdapterTest {

    private final UpdateSellableProductTerminalContextAdapter adapter =
            new UpdateSellableProductTerminalContextAdapter();

    @Test
    void from_whenNoWrites_shouldNotMarkPartial() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                new UpdateSellableProductContext.Product("Shirt", "cat-1", "NEW", "shirt", null),
                List.of(),
                List.of()
        );

        assertThat(adapter.workflowName()).isEqualTo(UpdateSellableProductWorkflowNames.WORKFLOW_NAME);
        assertThat(adapter.from(context)).isEqualTo(new WorkflowTerminalDetails("actor-1", "merchant-1", false));
    }

    @Test
    void from_whenCatalogWrote_shouldMarkPartial() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                new UpdateSellableProductContext.Product("Shirt", "cat-1", "NEW", "shirt", null),
                List.of(),
                List.of()
        ).withProductUpdated("product-1", List.of());

        assertThat(adapter.from(context).partiallyApplied()).isTrue();
    }
}

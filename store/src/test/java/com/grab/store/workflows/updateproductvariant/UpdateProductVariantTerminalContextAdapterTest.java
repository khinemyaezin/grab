package com.grab.store.workflows.updateproductvariant;

import com.grab.store.workflows.internal.service.WorkflowTerminalDetails;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantTerminalContextAdapter;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantWorkflowNames;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductVariantTerminalContextAdapterTest {

    private final UpdateProductVariantTerminalContextAdapter adapter =
            new UpdateProductVariantTerminalContextAdapter();

    @Test
    void from_whenNoWrites_shouldNotMarkPartial() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                null,
                List.of()
        );

        assertThat(adapter.workflowName()).isEqualTo(UpdateProductVariantWorkflowNames.WORKFLOW_NAME);
        assertThat(adapter.from(context)).isEqualTo(new WorkflowTerminalDetails("actor-1", "merchant-1", false));
    }

    @Test
    void from_whenCatalogWrote_shouldMarkPartial() {
        UpdateProductVariantContext context = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                null,
                List.of()
        ).withVariantUpdated("SKU-1");

        assertThat(adapter.from(context).partiallyApplied()).isTrue();
    }
}

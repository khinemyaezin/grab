package com.grab.store.workflows.updatesellableproduct;

import com.grab.store.workflows.internal.updatesellableproduct.UpdateSellableProductContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSellableProductContextTest {

    @Test
    void withProductUpdated_whenPricingLineHasSkuOnly_shouldAssignVariantIdFromEvent() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(pricingLine("SKU-NEW", null))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-new", "SKU-NEW")),
                List.of("SKU-NEW")
        );

        assertThat(updated.pricingLines()).hasSize(1);
        assertThat(updated.pricingLines().getFirst().sku()).isEqualTo("SKU-NEW");
        assertThat(updated.pricingLines().getFirst().variantId()).isEqualTo("variant-new");
        assertThat(updated.addedSkus()).containsExactly("SKU-NEW");
    }

    @Test
    void withProductUpdated_whenClientSentVariantId_shouldOverwriteFromCatalogEvent() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(pricingLine("SKU-1", "stale-variant"))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-1", "SKU-1")),
                List.of()
        );

        assertThat(updated.pricingLines().getFirst().variantId()).isEqualTo("variant-1");
    }

    @Test
    void withProductUpdated_whenNoMatchingSku_shouldLeavePricingLineUnchanged() {
        UpdateSellableProductContext context = UpdateSellableProductContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                product(),
                List.of(),
                List.of(pricingLine("SKU-1", null))
        );

        UpdateSellableProductContext updated = context.withProductUpdated(
                "product-1",
                List.of(new UpdateSellableProductContext.VariantRef("variant-2", "SKU-2")),
                List.of("SKU-2")
        );

        assertThat(updated.pricingLines().getFirst().variantId()).isNull();
        assertThat(updated.pricingLines().getFirst().sku()).isEqualTo("SKU-1");
    }

    private static UpdateSellableProductContext.Product product() {
        return new UpdateSellableProductContext.Product("Shirt", "cat-1", "NEW", "shirt", null);
    }

    private static UpdateSellableProductContext.PricingLine pricingLine(String sku, String variantId) {
        return new UpdateSellableProductContext.PricingLine(
                sku,
                variantId,
                "Base",
                "USD",
                new BigDecimal("19.99"),
                null,
                null,
                List.of()
        );
    }
}

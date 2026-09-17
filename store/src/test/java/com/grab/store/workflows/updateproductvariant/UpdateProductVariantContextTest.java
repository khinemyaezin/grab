package com.grab.store.workflows.updateproductvariant;

import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;
import com.grab.store.workflows.internal.workflows.updateproductvariant.UpdateProductVariantContext;
import com.inventory.domain.enums.AdjustmentReason;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductVariantContextTest {

    @Test
    void withVariantUpdated_shouldSetCatalogSkuWithoutChangingInputSku() {
        UpdateProductVariantContext context = sampleContext();

        UpdateProductVariantContext updated = context.withVariantUpdated("NEW-SKU");

        assertThat(updated.sku()).isEqualTo("SKU-1");
        assertThat(updated.catalogSku()).isEqualTo("NEW-SKU");
        assertThat(updated.skuForPrice()).isEqualTo("NEW-SKU");
        assertThat(updated.variantUpdated()).isTrue();
    }

    @Test
    void skuForPrice_whenCatalogSkuMissing_shouldFallBackToInputSku() {
        UpdateProductVariantContext context = sampleContext();

        assertThat(context.skuForPrice()).isEqualTo("SKU-1");
    }

    @Test
    void withPricePair_whenCreated_shouldRecordCreatedPriceSetId() {
        UpdateProductVariantContext context = sampleContext().withVariantUpdated("SKU-1");

        UpdateProductVariantContext updated = context.withPricePair(
                new UpdateProductVariantContext.PricePair("variant-1", "SKU-1", "price-set-new"),
                true
        );

        assertThat(updated.pricePair().priceSetId()).isEqualTo("price-set-new");
        assertThat(updated.createdPriceSetIds()).containsExactly("price-set-new");
    }

    @Test
    void withPricePair_whenNotCreated_shouldLeaveCreatedPriceSetIdsEmpty() {
        UpdateProductVariantContext context = sampleContext();

        UpdateProductVariantContext updated = context.withPricePair(
                new UpdateProductVariantContext.PricePair("variant-1", "SKU-1", "price-set-1"),
                false
        );

        assertThat(updated.createdPriceSetIds()).isEmpty();
    }

    @Test
    void hasPriceAndInventory_shouldFollowOptionalFields() {
        UpdateProductVariantContext withBoth = sampleContext();
        UpdateProductVariantContext catalogOnly = UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                null,
                null,
                List.of()
        );

        assertThat(withBoth.hasPrice()).isTrue();
        assertThat(withBoth.inventoryLines()).hasSize(1);
        assertThat(catalogOnly.hasPrice()).isFalse();
        assertThat(catalogOnly.inventoryLines()).isEmpty();
        assertThat(catalogOnly.priceSynced()).isTrue();
        assertThat(catalogOnly.allInventoryItemsSynced()).isTrue();
    }

    @Test
    void isPartiallyApplied_whenVariantUpdated_shouldBeTrue() {
        UpdateProductVariantContext context = sampleContext().withVariantUpdated("SKU-1");

        assertThat(context.isPartiallyApplied()).isTrue();
    }

    private static UpdateProductVariantContext sampleContext() {
        return UpdateProductVariantContext.createContext(
                "merchant-1",
                "actor-1",
                "MERCHANT_ACCOUNT",
                "merchant-1",
                "product-1",
                "variant-1",
                "SKU-1",
                true,
                new UpdateProductVariantContext.Price(
                        "Base",
                        "USD",
                        new BigDecimal("19.99"),
                        null,
                        null,
                        List.of()
                ),
                List.of(new UpdateProductVariantContext.InventoryLine(
                        "SKU-1",
                        "loc-1",
                        "inv-1",
                        InventorySyncOp.ADJUST,
                        null,
                        new InventorySyncPayload.AdjustStock(8, AdjustmentReason.CORRECTION),
                        null,
                        null,
                        null
                ))
        );
    }
}

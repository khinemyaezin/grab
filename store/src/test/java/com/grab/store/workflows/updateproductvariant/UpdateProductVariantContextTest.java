package com.grab.store.workflows.updateproductvariant;

import com.grab.store.workflows.internal.updateproductvariant.UpdateProductVariantContext;
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
        assertThat(updated.createdPriceSetId()).isEqualTo("price-set-new");
    }

    @Test
    void withPricePair_whenNotCreated_shouldLeaveCreatedPriceSetIdEmpty() {
        UpdateProductVariantContext context = sampleContext();

        UpdateProductVariantContext updated = context.withPricePair(
                new UpdateProductVariantContext.PricePair("variant-1", "SKU-1", "price-set-1"),
                false
        );

        assertThat(updated.createdPriceSetId()).isNull();
    }

    @Test
    void hasPriceAndAdjustStock_shouldFollowOptionalFields() {
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
                null
        );

        assertThat(withBoth.hasPrice()).isTrue();
        assertThat(withBoth.hasAdjustStock()).isTrue();
        assertThat(catalogOnly.hasPrice()).isFalse();
        assertThat(catalogOnly.hasAdjustStock()).isFalse();
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
                new UpdateProductVariantContext.Price(
                        "Base",
                        "USD",
                        new BigDecimal("19.99"),
                        null,
                        null,
                        List.of()
                ),
                new UpdateProductVariantContext.AdjustStock("inv-1", 8, AdjustmentReason.CORRECTION)
        );
    }
}

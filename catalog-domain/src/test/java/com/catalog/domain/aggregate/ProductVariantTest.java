package com.catalog.domain.aggregate;

import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVariantTest {

    @Test
    void create_shouldDefaultManageInventoryToFalse() {
        ProductVariant variant = ProductVariant.create(
                new CommonId("v1"),
                "SKU001",
                List.of(new ProductVariation(new CommonId("red"), new CommonId("color")))
        );

        assertThat(variant.isManageInventory()).isFalse();
    }

    @Test
    void create_shouldHonorExplicitManageInventory() {
        ProductVariant variant = ProductVariant.create(
                new CommonId("v1"),
                "SKU001",
                List.of(new ProductVariation(new CommonId("red"), new CommonId("color"))),
                true
        );

        assertThat(variant.isManageInventory()).isTrue();
    }
}

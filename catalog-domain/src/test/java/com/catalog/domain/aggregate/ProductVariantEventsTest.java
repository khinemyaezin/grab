package com.catalog.domain.aggregate;

import com.catalog.domain.event.ProductVariantAddedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.domain.Event;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVariantEventsTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.create(new CommonId("product-1"), new CommonId("merchant-1"), "T-Shirt", new CommonId("clothing"));
    }

    @Test
    void addVariant_shouldRaiseProductVariantAddedEvent() {
        var variant = ProductVariant.create(new CommonId("v1"), "SKU001", List.of(
                new ProductVariation(new CommonId("red"), new CommonId("color"))
        ));

        product.addVariant(variant);

        List<Event> events = product.pullEvents();
        assertThat(events).containsExactly(new ProductVariantAddedEvent(
                new CommonId("product-1"),
                new CommonId("v1"),
                "SKU001",
                "T-Shirt"
        ));
    }

    @Test
    void updateVariant_shouldRaiseProductVariantChangeEventWithProductAndVariantIds() {
        var variant = ProductVariant.create(new CommonId("v1"), "SKU001", List.of(
                new ProductVariation(new CommonId("red"), new CommonId("color"))
        ));
        product.addVariant(variant);
        product.pullEvents();

        var updated = new ProductVariant(new CommonId("v1"), "SKU002", ProductVariantStatus.ACTIVE, List.of(
                new ProductVariation(new CommonId("red"), new CommonId("color"))
        ));
        boolean ok = product.updateVariant(variant, updated);

        assertThat(ok).isTrue();
        assertThat(product.pullEvents()).containsExactly(new ProductVariantChangeEvent(
                new CommonId("product-1"),
                new CommonId("v1"),
                "SKU002"
        ));
    }
}

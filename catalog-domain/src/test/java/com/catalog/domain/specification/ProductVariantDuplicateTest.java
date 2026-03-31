package com.catalog.domain.specification;

import com.catalog.domain.aggregate.*;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.VariantTypeStatus;
import com.grab.framework.id.Id;
import com.catalog.domain.valueobject.ProductVariation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ProductVariantDuplicateTest {
    private Product product;

    static class CommonId implements Id {
        private final String id;
        public CommonId(String id) {
            this.id = id;
        }
        @Override
        public String getValue() {
            return this.id;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CommonId commonId)) return false;
            return Objects.equals(id, commonId.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    @BeforeEach
    void setUp() {
        product =  Product.create(new CommonId("product-1"), "T-Shirt", new CommonId("clothing"));

    }

    @Test
    void shouldNotAddDuplicateVariantWithSameSKU() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"),  "SKU001",  ProductVariantStatus.ACTIVE,List.of(
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));

        // Create duplicate variant with same SKU but different ID
        var variant2 = new ProductVariant(new CommonId("v2"),  "SKU001", ProductVariantStatus.ACTIVE, List.of(
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));

        assertTrue(product.addVariant(variant1));
        assertFalse(product.addVariant(variant2));
        assertEquals(1, product.getVariants().size());
    }

    @Test
    void testAdded_sameVariations_returnFalse() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"),  "SKU001", ProductVariantStatus.ACTIVE, List.of(
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));

        // Create duplicate variant with different SKU but same variations
        var variant2 = new ProductVariant(new CommonId("v2"),  "SKU002",  ProductVariantStatus.ACTIVE,List.of(
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));

        product.addVariant(variant1);
        assertFalse(product.addVariant(variant2));
    }

    @Test
    void shouldNotAddDuplicateVariantWithSameVariationsInDifferentOrder() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"),  "SKU001", ProductVariantStatus.ACTIVE, List.of(
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));

        // Create duplicate variant with variations in different order
        var variant2 = new ProductVariant(new CommonId("v2"),  "SKU002",  ProductVariantStatus.ACTIVE,List.of(
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size")),
                new ProductVariation("Red", new CommonId("red"), "Color", new CommonId("color"))
        ));

        assertTrue(product.addVariant(variant1));
        assertFalse(product.addVariant(variant2));
        assertEquals(1, product.getVariants().size());
    }
    @Test
    void shouldNotAddDuplicateVariantWithSameSku() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"),  "SKU001", ProductVariantStatus.ACTIVE, List.of(
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));

        // Create duplicate variant with variations in different order
        var variant2 = new ProductVariant(new CommonId("v2"),  "SKU001",  ProductVariantStatus.ACTIVE,List.of(
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size")),
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color"))
        ));

        assertTrue(product.addVariant(variant1));
        assertFalse(product.addVariant(variant2));
        assertEquals(1, product.getVariants().size());
    }


    @Test
    void shouldAddVariantWithDifferentVariations() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"),  "SKU001", ProductVariantStatus.ACTIVE, List.of(
                new ProductVariation("Red", new CommonId("red"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));

        // Create second variant with different color
        var variant2 = new ProductVariant(new CommonId("v2"),  "SKU002", ProductVariantStatus.ACTIVE, List.of(
                new ProductVariation("Blue", new CommonId("blue"), "Color",   new CommonId("color")),
                new ProductVariation("Small", new CommonId("small"), "Size",new CommonId("size"))
        ));
        assertTrue(product.addVariant(variant1));
        assertTrue(product.addVariant(variant2));
        assertEquals(2, product.getVariants().size());
    }
} 
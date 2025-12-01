package com.product.domain.specification;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.aggregate.product.VariantType;
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
        // Setup color variant type
        VariantType color = new VariantType(new CommonId("color"), "Color");
        color.addOption(new VariantOption(new CommonId("red"), "Red", color));
        color.addOption(new VariantOption(new CommonId("blue"), "Blue", color));

        // Setup size variant type
        VariantType size = new VariantType(new CommonId("size"), "Size");
        size.addOption(new VariantOption(new CommonId("small"), "Small", size));
        size.addOption(new VariantOption(new CommonId("medium"), "Medium", size));

        // Create product
        product = new Product(new CommonId("product-1"), "T-Shirt", new CommonId("clothing"));
    }

    @Test
    void shouldNotAddDuplicateVariantWithSameSKU() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"), product.getId(), "SKU001", List.of(
                new ProductVariation("Red", new CommonId("red"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));

        // Create duplicate variant with same SKU but different ID
        var variant2 = new ProductVariant(new CommonId("v2"), product.getId(), "SKU001", List.of(
                new ProductVariation("Red", new CommonId("red"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));

        assertTrue(product.addVariant(variant1));
        assertFalse(product.addVariant(variant2));
        assertEquals(1, product.getVariants().size());
    }

    @Test
    void testAdded_sameVariations_returnFalse() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"), product.getId(), "SKU001", List.of(
                new ProductVariation("Red", new CommonId("red"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));

        // Create duplicate variant with different SKU but same variations
        var variant2 = new ProductVariant(new CommonId("v2"), product.getId(), "SKU002", List.of(
                new ProductVariation("Red", new CommonId("red"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));

        product.addVariant(variant1);
        assertFalse(product.addVariant(variant2));
    }

    @Test
    void shouldNotAddDuplicateVariantWithSameVariationsInDifferentOrder() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"), product.getId(), "SKU001", List.of(
                new ProductVariation("Red", new CommonId("red"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));

        // Create duplicate variant with variations in different order
        var variant2 = new ProductVariant(new CommonId("v2"), product.getId(), "SKU002", List.of(
                new ProductVariation("Small", new CommonId("small"), "Size"),
                new ProductVariation("Red", new CommonId("red"), "Color")
        ));

        assertTrue(product.addVariant(variant1));
        assertFalse(product.addVariant(variant2));
        assertEquals(1, product.getVariants().size());
    }
    @Test
    void shouldNotAddDuplicateVariantWithSameSku() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"), product.getId(), "SKU001", List.of(
                new ProductVariation("Red", new CommonId("red"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));

        // Create duplicate variant with variations in different order
        var variant2 = new ProductVariant(new CommonId("v2"), product.getId(), "SKU001", List.of(
                new ProductVariation("Small", new CommonId("small"), "Size"),
                new ProductVariation("Red", new CommonId("red"), "Color")
        ));

        assertTrue(product.addVariant(variant1));
        assertFalse(product.addVariant(variant2));
        assertEquals(1, product.getVariants().size());
    }


    @Test
    void shouldAddVariantWithDifferentVariations() {
        // Create first variant
        var variant1 = new ProductVariant(new CommonId("v1"), product.getId(), "SKU001", List.of(
                new ProductVariation("Red", new CommonId("red"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));

        // Create second variant with different color
        var variant2 = new ProductVariant(new CommonId("v2"), product.getId(), "SKU002", List.of(
                new ProductVariation("Blue", new CommonId("blue"), "Color"),
                new ProductVariation("Small", new CommonId("small"), "Size")
        ));
        assertTrue(product.addVariant(variant1));
        assertTrue(product.addVariant(variant2));
        assertEquals(2, product.getVariants().size());
    }
} 
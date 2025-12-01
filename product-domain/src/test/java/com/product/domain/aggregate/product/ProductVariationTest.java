package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class ProductVariationTest {
    static class CommonId implements Id {
        private final String id;
        public CommonId(String id) {
            this.id = id;
        }
        public CommonId() {
            this.id = UUID.randomUUID().toString();
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

    private Id productId;
    private Id variantId1;
    private Id variantId2;
    private ProductVariation variationSizeL;
    private ProductVariation variationColorRed;

    @BeforeEach
    void setUp() {
        // Common objects used in multiple tests
        productId = new CommonId();
        variantId1 = new CommonId();
        variantId2 = new CommonId();
        variationSizeL = new ProductVariation("L", null, "Size");
        variationColorRed = new ProductVariation("Red", null, "Color");
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU123", List.of(variationSizeL));
        assertEquals(variant1, variant1, "An object must be equal to itself.");
    }

    @Test
    void testEquals_sameProductIdAndSku_returnsTrue() {
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU123", List.of(variationSizeL));
        // Different variantId, different SKU casing, different variations list
        ProductVariant variant2 = new ProductVariant(variantId2, productId, "sku123", List.of(variationColorRed));

        assertTrue(variant1.equals(variant2), "Objects with the same productId and case-insensitive SKU should be equal.");
    }

    @Test
    void testEquals_sameProductIdAndVariations_returnsTrue() {
        List<ProductVariation> variations = List.of(variationSizeL, variationColorRed);
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU-ABC", variations);
        // Different variantId, different SKU, but same variations
        ProductVariant variant2 = new ProductVariant(variantId2, productId, "SKU-XYZ", variations);

        assertTrue(variant1.equals(variant2), "Objects with the same productId and variations should be equal.");
    }

    @Test
    void testEquals_allFieldsMatch_returnsTrue() {
        List<ProductVariation> variations = List.of(variationSizeL, variationColorRed);
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU123", variations);
        ProductVariant variant2 = new ProductVariant(variantId2, productId, "sku123", variations);

        assertTrue(variant1.equals(variant2), "Objects should be equal when productId, SKU, and variations match.");
    }


    @Test
    void testEquals_differentProductId_returnsFalse() {
        Id otherProductId = new CommonId();
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU123", List.of(variationSizeL));
        ProductVariant variant2 = new ProductVariant(variantId2, otherProductId, "SKU123", List.of(variationSizeL));

        assertNotEquals(variant1, variant2, "Objects with different productIds should not be equal.");
    }

    @Test
    void testEquals_differentSkuAndVariations_returnsFalse() {
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU-ABC", List.of(variationSizeL));
        ProductVariant variant2 = new ProductVariant(variantId2, productId, "SKU-XYZ", List.of(variationColorRed));

        assertNotEquals(variant1, variant2, "Objects with different SKUs and different variations should not be equal.");
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU123", Collections.emptyList());
        assertFalse(variant1.equals(null), "Comparison with null must return false.");
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        ProductVariant variant1 = new ProductVariant(variantId1, productId, "SKU123", Collections.emptyList());
        Object otherObject = new Object();
        assertFalse(variant1.equals(otherObject), "Comparison with an object of a different type must return false.");
    }
}
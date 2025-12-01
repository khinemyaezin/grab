package com.product.domain.factory;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariation;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * Test helper that builds a Product aggregate using the demo product-full.json data.
 * Constructed manually to avoid JSON parsing in unit tests.
 */
public class ProductTestData {
    public static Product fullProduct() {
        Id productId = id("product-1");
        Product product = new Product(productId, "Unisex Tee", id("category-1"));

        product.addVariant(variant("1", productId, "SKU-LYM",
                variation("size-large", "Large", "Size"),
                variation("color-yellow", "Yellow", "Color"),
                variation("gender-male", "Male", "Gender")));

        product.addVariant(variant("2", productId, "SKU-LYF",
                variation("size-large", "Large", "Size"),
                variation("color-yellow", "Yellow", "Color"),
                variation("gender-female", "Female", "Gender")));

        product.addVariant(variant("3", productId, "SKU-LRM",
                variation("size-large", "Large", "Size"),
                variation("color-red", "Red", "Color"),
                variation("gender-male", "Male", "Gender")));

        product.addVariant(variant("4", productId, "SKU-LRF",
                variation("size-large", "Large", "Size"),
                variation("color-red", "Red", "Color"),
                variation("gender-female", "Female", "Gender")));

        product.addVariant(variant("5", productId, "SKU-SYM",
                variation("size-small", "Small", "Size"),
                variation("color-yellow", "Yellow", "Color"),
                variation("gender-male", "Male", "Gender")));

        product.addVariant(variant("6", productId, "SKU-SYF",
                variation("size-small", "Small", "Size"),
                variation("color-yellow", "Yellow", "Color"),
                variation("gender-female", "Female", "Gender")));

        product.addVariant(variant("7", productId, "SKU-SRM",
                variation("size-small", "Small", "Size"),
                variation("color-red", "Red", "Color"),
                variation("gender-male", "Male", "Gender")));

        product.addVariant(variant("8", productId, "SKU-SRF",
                variation("size-small", "Small", "Size"),
                variation("color-red", "Red", "Color"),
                variation("gender-female", "Female", "Gender")));

        return product;
    }

    protected static ProductVariant variant(String variantId, Id productId, String sku, ProductVariation... variations) {
        return new ProductVariant(id(variantId), productId, sku, List.of(variations));
    }

    protected static ProductVariation variation(String optionId, String optionName, String typeName) {
        ProductVariation variation = new ProductVariation(optionName, typeName);
        setOptionId(variation, optionId);
        return variation;
    }

    protected static void setOptionId(ProductVariation variation, String optionId) {
        try {
            Field field = ProductVariation.class.getDeclaredField("optionId");
            field.setAccessible(true);
            field.set(variation, id(optionId));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set optionId on ProductVariation", e);
        }
    }

    protected static Id id(String value) {
        return new CommonId(value);
    }

    protected static final class CommonId implements Id {
        private final String id;

        CommonId(String id) {
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

        @Override
        public String toString() {
            return id;
        }
    }
}

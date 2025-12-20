package com.product.domain.factory;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Test helper that builds a Product aggregate using the demo product-full.json data.
 * Constructed manually to avoid JSON parsing in unit tests.
 */
public class ProductTestData {
    public static Product fullProduct() {
        Id productId = id("product-1");
        Product product = new Product(productId, "Unisex Tee", id("category-1"));

        product.addVariant(variant("1", productId, "SKU-LYM",
                variation(id("size-large"), "Large", new CommonId("size"), "Size"),
                variation(id("color-yellow"), "Yellow", new CommonId("color"), "Color"),
                variation(id("gender-male"), "Male", new CommonId("gender"), "Gender")));

        product.addVariant(variant("2", productId, "SKU-LYF",
                variation(id("size-large"), "Large",new CommonId("size"), "Size"),
                variation(id("color-yellow"), "Yellow",  new CommonId("color"), "Color"),
                variation(id("gender-female"), "Female",  new CommonId("gender"), "Gender")));

        product.addVariant(variant("3", productId, "SKU-LRM",
                variation(id("size-large"), "Large", new CommonId("size"), "Size"),
                variation(id("color-red"), "Red",  new CommonId("color"), "Color"),
                variation(id("gender-male"), "Male",  new CommonId("gender"), "Gender")));

        product.addVariant(variant("4", productId, "SKU-LRF",
                variation(id("size-large"), "Large", new CommonId("size"), "Size"),
                variation(id("color-red"), "Red",  new CommonId("color"), "Color"),
                variation(id("gender-female"), "Female",  new CommonId("gender"), "Gender")));

        product.addVariant(variant("5", productId, "SKU-SYM",
                variation(id("size-small"), "Small", new CommonId("size"), "Size"),
                variation(id("color-yellow"), "Yellow",  new CommonId("color"), "Color"),
                variation(id("gender-male"), "Male",  new CommonId("gender"), "Gender")));

        product.addVariant(variant("6", productId, "SKU-SYF",
                variation(id("size-small"), "Small", new CommonId("size"), "Size"),
                variation(id("color-yellow"), "Yellow",  new CommonId("color"), "Color"),
                variation(id("gender-female"), "Female",  new CommonId("gender"), "Gender")));

        product.addVariant(variant("7", productId, "SKU-SRM",
                variation(id("size-small"), "Small", new CommonId("size"), "Size"),
                variation(id("color-red"), "Red",  new CommonId("color"), "Color"),
                variation(id("gender-male"), "Male",  new CommonId("gender"), "Gender")));

        product.addVariant(variant("8", productId, "SKU-SRF",
                variation(id("size-small"), "Small", new CommonId("size"), "Size"),
                variation(id("color-red"), "Red",  new CommonId("color"), "Color"),
                variation(id("gender-female"), "Female",  new CommonId("gender"), "Gender")));

        return product;
    }

    protected static ProductVariant variant(String variantId, Id productId, String sku, ProductVariation... variations) {
        return new ProductVariant(id(variantId), productId, sku, List.of(variations));
    }

    protected static ProductVariation variation(Id optionId, String optionName,Id typeId, String typeName) {
        return new ProductVariation(optionName, optionId, typeName, typeId);
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

    public static Product emptyVariationProduct() {
        Id productId = id("product-1");
        return new Product(productId, "Unisex Tee", id("category-1"));
    }
    
    protected static String getSku(List<ProductVariation> variations) {
        return variations.stream()
                .map(variation -> variation.getOptionName().substring(0, 1).toUpperCase() + variation.getOptionName().substring(1))
                .collect(Collectors.joining("-"));
    }
}

package com.product.domain.entity.product;

import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.entity.variant_option.VariantOption;
import com.product.domain.entity.variant_type.VariantType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductTest {

    @Test
    void extractVariantTypes() {

        var color = new VariantType("color", "Color");
        color.addOption(new VariantOption("yellow", "Yellow", color));
        color.addOption(new VariantOption("blue", "Blue", color));

        var size = new VariantType("size", "Size");
        size.addOption(new VariantOption("small", "Small", size));

        var packages = new VariantType("package", "Package");
        packages.addOption(new VariantOption("128", "128", packages));
        packages.addOption(new VariantOption("256", "256", packages));

        Product p = new Product("product-1", "Shirt", "clothing");

        var variant1 = new ProductVariant("v1", "product-1", "sku1", List.of(
                new ProductVariation(new VariantOption("yellow", "Yellow", color)),
                new ProductVariation(new VariantOption("small", "Small", size)),
                new ProductVariation(new VariantOption("128", "128", packages))
        ));
        var variant2 = new ProductVariant("v2", "product-1", "sku2", List.of(
                new ProductVariation(new VariantOption("blue", "Blue", color)),
                new ProductVariation(new VariantOption("small", "Small", size)),
                new ProductVariation(new VariantOption("128", "128", packages))
        ));
        var variant3 = new ProductVariant("v3", "product-1", "sku3", List.of(
                new ProductVariation(new VariantOption("yellow", "Yellow", color)),
                new ProductVariation(new VariantOption("small", "Small", size)),
                new ProductVariation(new VariantOption("256", "256", packages))
        ));

        var variant4 = new ProductVariant("v4", "product-1", "sku4", List.of(
                new ProductVariation(new VariantOption("blue", "Blue", color)),
                new ProductVariation(new VariantOption("small", "Small", size)),
                new ProductVariation(new VariantOption("256", "256", packages))
        ));

        p.addVariant(variant1);
        p.addVariant(variant2);
        p.addVariant(variant3);
        p.addVariant(variant4);

        assertEquals(4, p.getVariants().size());
    }

    @Test
    void shouldNotAddDuplicateVariation() {

        var color = new VariantType("color", "Color");
        color.addOption(new VariantOption("yellow", "Yellow", color));
        color.addOption(new VariantOption("blue", "Blue", color));

        var size = new VariantType("size", "Size");
        size.addOption(new VariantOption("small", "Small", size));

        var packages = new VariantType("package", "Package");
        packages.addOption(new VariantOption("128", "128", packages));
        packages.addOption(new VariantOption("256", "256", packages));

        Product p = new Product("product-1", "Shirt", "clothing");

        var variant1 = new ProductVariant("v1", "product-1", "sku1", List.of(
                new ProductVariation(new VariantOption("yellow", "Yellow", color)),
                new ProductVariation(new VariantOption("small", "Small", size)),
                new ProductVariation(new VariantOption("128", "128", packages))
        ));
        var variant2 = new ProductVariant("v2", "product-1", "sku2", List.of(
                new ProductVariation(new VariantOption("yellow", "Yellow", color)),
                new ProductVariation(new VariantOption("small", "Small", size)),
                new ProductVariation(new VariantOption("128", "128", packages))
        ));

        p.addVariant(variant1);
        p.addVariant(variant2);

        assertEquals(1, p.getVariants().size());
    }
}
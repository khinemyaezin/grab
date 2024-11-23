package com.product.domain.specification;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.entity.variant_option.VariantOption;
import com.product.domain.entity.variant_type.VariantType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UniqueProductVariantCompositeSpecTest {

    @Test
    void shouldAddVariant() {
        var color = new VariantType("color", "Color");
        color.addOption(new VariantOption("yellow", "Yellow", color));

        Product p = new Product("product-1", "Shirt", "clothing");

        var variant1 = new ProductVariant("v1", "product-1", "sku1", List.of(
                new ProductVariation(new VariantOption("yellow", "Yellow", color))
        ));

        p.addVariant(variant1);

        assertEquals(1, p.getVariants().size());
        assertTrue(p.getVariantTypes().contains(new VariantType("color", "Color")));
    }

    @Test
    void shouldAddVariantWhenReverseOrder() {
        var color = new VariantType("color", "Color");
        color.addOption(new VariantOption("yellow", "Yellow", color));
        color.addOption(new VariantOption("blue", "Blue", color));

        Product p = new Product("product-1", "Shirt", "clothing");

        var variant1 = new ProductVariant("v1", "product-1", "sku1", List.of(
                new ProductVariation( new VariantOption("yellow", "Yellow", color))
        ));
        var variant2 = new ProductVariant("v2", "product-1", "sku2", List.of(
                new ProductVariation( new VariantOption("blue", "Blue", color))
        ));

        p.addVariant(variant2);
        p.addVariant(variant1);

        assertEquals(2, p.getVariants().size());

    }

    @Test
    void shouldAddVariantWithColorAndSize() {
        var color = new VariantType("color", "Color");
        color.addOption(new VariantOption("yellow", "Yellow", color));
        color.addOption(new VariantOption("blue", "Blue", color));

        var size = new VariantType("size", "Size");
        size.addOption(new VariantOption("small", "Small", size));

        Product p = new Product("product-1", "Shirt", "clothing");

        var variant1 = new ProductVariant("v1", "product-1", "sku1", List.of(
                new ProductVariation(new VariantOption("yellow", "Yellow", color)),
                new ProductVariation( new VariantOption("small", "Small", size))
        ));
        var variant2 = new ProductVariant("v2", "product-1", "sku2", List.of(
                new ProductVariation( new VariantOption("blue", "Blue", color)),
                new ProductVariation(new VariantOption("small", "Small", size))
        ));

        p.addVariant(variant1);
        p.addVariant(variant2);

        assertEquals(2, p.getVariants().size());

    }



}
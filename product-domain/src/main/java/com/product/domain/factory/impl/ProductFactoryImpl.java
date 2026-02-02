package com.product.domain.factory.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.factory.ProductFactory;
import com.product.domain.service.SkuGenerator;
import com.product.domain.service.VariantCombination;
import com.product.domain.valueobject.ProductVariation;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class ProductFactoryImpl implements ProductFactory {
    private final VariantCombination variantCombination;
    private final IdGenerator idGenerator;
    private final SkuGenerator skuGenerator;

    public Product create(ProductSpec spec) {
        Id productId = idGenerator.generateId();
        Product product = new Product(productId,spec.name(), spec.categoryId());

        List<List<VariantOption>> combinations =
                variantCombination.generateCombinations(spec.desiredVariantTypes());

        for (List<VariantOption> combination : combinations) {
            List<ProductVariation> variations = toVariations(combination);
            String sku = generateSku(product, variations);
            Id newVariantId = idGenerator.generateId();
            ProductVariant productVariant = new ProductVariant(newVariantId, product.getId(), sku, variations);
            product.addVariant(productVariant);
        }

        return product;
    }

    private List<ProductVariation> toVariations(List<VariantOption> options) {
        return options.stream()
                .map(this::toVariation)
                .toList();
    }

    private ProductVariation toVariation(VariantOption option) {
        VariantType type = Objects.requireNonNull(option.getVariantType(),
                "VariantType is required for option: " + option.getId());
        return new ProductVariation(
                option.getName(),
                option.getId(),
                type.getName(),
                type.getId()
        );
    }

    private String generateSku(Product product, List<ProductVariation> variations) {
        SkuGenerator.Context skuContext = new SkuGenerator.Context(
                product.getId(),
                product.getName(),
                variations,
                null,
                0,
                Map.of());
        return skuGenerator.generate(skuContext);
    }
}

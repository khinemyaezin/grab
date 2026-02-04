package com.product.infrastructure.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.*;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductTestData {
    protected Product createProduct() {
        Id clothing = id("clothing-category");

        VariantType colorType = new VariantType(id("color"), "Color");
        colorType.addOption(new VariantOption(id("color-red"), "Red", colorType));
        colorType.addOption(new VariantOption(id("color-blue"), "Blue", colorType));

        VariantType sizeType = new VariantType(id("size"), "Size");
        sizeType.addOption(new VariantOption(id("size-xs"), "XS", sizeType));
        sizeType.addOption(new VariantOption(id("size-s"), "S", sizeType));

        Product product = Product.create(id("product-tshirt"), "Premium Cotton T-Shirt", clothing);

        for (VariantOption color : colorType.getOptions()) {
            for (VariantOption size : sizeType.getOptions()) {
                String sku = "TSH-" + color.getName().toUpperCase() + "-" + size.getName().toUpperCase();
                
                List<ProductVariation> variations = new ArrayList<>();
                variations.add(new ProductVariation(color.getName(),color.getId(), colorType.getName(), colorType.getId()));
                variations.add(new ProductVariation(size.getName(), size.getId(), sizeType.getName(), sizeType.getId()));

                ProductVariant variant = new ProductVariant(
                    id(UUID.randomUUID().toString()),
                    product.getId(),
                    sku,
                    variations
                );

                product.addVariant(variant);
            }
        }

        return product;
    }

    protected ProductEntity createProductEntity(Product product) {
        Id clothing = id(UUID.randomUUID().toString());

        ProductEntity entity = new ProductEntity();
        entity.setCategoryId(clothing.getValue());
        entity.setUuid(product.getId().getValue()); // Use the product's UUID
        entity.setName(product.getName());

        // Create ProductVariantEntity objects for each variant
        for (ProductVariant variant : product.getVariants()) {
            ProductVariantEntity variantEntity = getProductVariantEntity(variant, entity);
            entity.addVariant(variantEntity);
        }
        return entity;
    }

    protected ProductVariantEntity getProductVariantEntity(ProductVariant variant, ProductEntity entity) {
        ProductVariantEntity variantEntity = new ProductVariantEntity();
        variantEntity.setId(System.currentTimeMillis());
        variantEntity.setUuid(variant.getId().getValue());
        variantEntity.setSku(variant.getSku());
        variantEntity.setProduct(entity);

        // Create ProductVariationEntity objects for each variation
        for (ProductVariation variation : variant.getVariations()) {
            ProductVariationEntity variationEntity = new ProductVariationEntity();
            variationEntity.setProductVariant(variantEntity);
        }
        return variantEntity;
    }

    protected static Id id(String value) {
        return new CommonId(value);
    }

    private static final class CommonId implements Id {
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
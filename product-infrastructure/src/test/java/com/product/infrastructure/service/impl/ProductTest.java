package com.product.infrastructure.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.*;
import com.product.infrastructure.common.CommonId;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import java.util.*;

public class ProductTest {
    protected Product createProduct() {
        Id clothing = new CommonId();

        VariantType colorType = new VariantType(new CommonId(), "Color");
        colorType.addOption(new VariantOption(new CommonId(), "Red", colorType));
        colorType.addOption(new VariantOption(new CommonId(), "Blue", colorType));

        VariantType sizeType = new VariantType(new CommonId(), "Size");
        sizeType.addOption(new VariantOption(new CommonId(), "XS", sizeType));
        sizeType.addOption(new VariantOption(new CommonId(), "S", sizeType));

        Product product = new Product(new CommonId(), "Premium Cotton T-Shirt", clothing);

        for (VariantOption color : colorType.getOptions()) {
            for (VariantOption size : sizeType.getOptions()) {
                String sku = "TSH-" + color.getName().toUpperCase() + "-" + size.getName().toUpperCase();
                
                List<ProductVariation> variations = new ArrayList<>();
                variations.add(new ProductVariation(color.getName(), colorType.getName()));
                variations.add(new ProductVariation(size.getName(), sizeType.getName()));

                ProductVariant variant = new ProductVariant(
                    new CommonId(),
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
        Id clothing = new CommonId();

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
            variationEntity.setVariantOptionValue(variation.getOptionName());
            variationEntity.setVariantTypeValue(variation.getTypeName());
        }
        return variantEntity;
    }
} 
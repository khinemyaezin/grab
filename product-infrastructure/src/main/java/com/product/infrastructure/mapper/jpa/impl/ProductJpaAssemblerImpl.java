package com.product.infrastructure.mapper.jpa.impl;

import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.*;
import com.product.infrastructure.mapper.jpa.*;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ProductJpaAssemblerImpl implements ProductJpaAssembler {
    private final ProductEntityMapper productEntityMapper;
    private final ProductVariantEntityMapper variantEntityMapper;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductVariationMapper productVariationMapper;

    public ProductEntity toFullEntityGraph(Product product, ProductEntity entity) {
        if(entity == null) {
            // Create new entity
            entity = toProductEntity(product);
            for(ProductVariant productVariant : product.getVariants()) {
                ProductVariantEntity variantEntity = toProductVariantEntity(productVariant);
                for(ProductVariation variation: productVariant.getVariations()) {
                    ProductVariationEntity variationEntity = toProductVariationEntity(variation);
                    variantEntity.addProductVariation(variationEntity);
                }
                entity.addVariant(variantEntity);
            }
        } else {
            // Merge into existing entity
            mergeProductEntity(entity, product);
            mergeVariants(entity, product.getVariants());
        }
        return entity;
    }

    private ProductEntity toProductEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        productEntityMapper.toEntity(product, entity);
        return entity;
    }

    private void mergeProductEntity(ProductEntity productEntity, Product product) {
        productEntityMapper.toEntity(product, productEntity);
    }

    private ProductVariantEntity toProductVariantEntity(ProductVariant variant) {
        ProductVariantEntity variantEntity = new ProductVariantEntity();
        variantEntityMapper.map(variant, variantEntity);
        return variantEntity;
    }

    private void mergeProductVariantEntity(ProductVariantEntity entity, ProductVariant variant) {
        variantEntityMapper.map(variant, entity);
    }

    private ProductVariationEntity toProductVariationEntity(ProductVariation productVariation) {
        return new ProductVariationEntity(
                new ProductVariationEntity.ProductVariationId(
                        productVariation.getOptionId().getValue(),
                        productVariation.getTypeId().getValue()
                ),
                null,
                productVariation.getOptionName(),
                productVariation.getTypeName()
        );
    }

    private void mergeVariants(ProductEntity productEntity, List<ProductVariant> domainVariants) {
        Map<String, ProductVariantEntity> existingByUuid = productEntity.getProductVariants().stream()
                .collect(Collectors.toMap(ProductVariantEntity::getUuid, Function.identity()));

        Set<String> processedUuids = new HashSet<>();
        List<ProductVariantEntity> resultVariants = new ArrayList<>();

        for (ProductVariant variantDomain : domainVariants) {
            String uuid = variantDomain.getId().getValue();
            ProductVariantEntity variantEntity = existingByUuid.get(uuid);

            if (variantEntity != null) {
                variantEntity.getProductVariations().clear();
                mergeProductVariantEntity(variantEntity, variantDomain);
                for(ProductVariation variation: variantDomain.getVariations()) {
                    ProductVariationEntity variationEntity = toProductVariationEntity(variation);
                    variantEntity.addProductVariation(variationEntity);
                }
                processedUuids.add(uuid);
                resultVariants.add(variantEntity);
            } else {
                // Create new variant
                ProductVariantEntity productVariantEntity = toProductVariantEntity(variantDomain);
                for(ProductVariation variation: variantDomain.getVariations()) {
                    ProductVariationEntity variationEntity = toProductVariationEntity(variation);
                    productVariantEntity.addProductVariation(variationEntity);
                }
                resultVariants.add(productVariantEntity);
            }
        }
        productEntity.getProductVariants().removeIf(e -> !processedUuids.contains(e.getUuid()));

        resultVariants.stream()
                .filter(e -> !existingByUuid.containsKey(e.getUuid()))
                .forEach(productEntity::addVariant);
    }

    public Product toFullDomainGraph(ProductEntity productJpaEntity){
        List<ProductVariant> productVariants = new ArrayList<>();

        for(ProductVariantEntity variantEntity : productJpaEntity.getProductVariants()) {
            List<ProductVariation> productVariations = new ArrayList<>();

            for( ProductVariationEntity variationEntity: variantEntity.getProductVariations()) {
                ProductVariation variation = productVariationMapper.toDomain(variationEntity);
                productVariations.add(variation);
            }

            ProductVariant variant = productVariantMapper.toDomain(variantEntity, productVariations);
            productVariants.add(variant);
        }

        return productMapper.toDomain(productJpaEntity, productVariants);
    }
}

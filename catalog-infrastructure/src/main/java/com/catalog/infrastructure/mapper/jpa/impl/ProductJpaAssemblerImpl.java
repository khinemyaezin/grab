package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.entity.ProductVariationEntity;
import com.catalog.infrastructure.mapper.jpa.*;
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

    public ProductEntity buildFullEntityGraph(Product product, ProductEntity entity) {
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
        variantEntityMapper.toEntity(variant, variantEntity);
        return variantEntity;
    }

    private void mergeProductVariantEntity(ProductVariantEntity entity, ProductVariant variant) {
        variantEntityMapper.toEntity(variant, entity);
    }

    private ProductVariationEntity toProductVariationEntity(ProductVariation productVariation) {
        return new ProductVariationEntity(
                new ProductVariationEntity.ProductVariationId(
                        productVariation.getOptionId().getValue(),
                        productVariation.getTypeId().getValue(),
                        null
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
                mergeProductVariantEntity(variantEntity, variantDomain);
                mergeVariations(variantEntity, variantDomain.getVariations());
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

    private void mergeVariations(ProductVariantEntity variantEntity, Set<ProductVariation> domainVariations) {
        Map<String, ProductVariationEntity> existingByKey = variantEntity.getProductVariations().stream()
                .collect(Collectors.toMap(
                        v -> variationKey(v.getId().getVariantOptionUuid(), v.getId().getVariantTypeUuid()),
                        Function.identity()
                ));

        Set<String> domainKeys = new HashSet<>();

        for (ProductVariation variation : domainVariations) {
            String key = variationKey(variation.getOptionId().getValue(), variation.getTypeId().getValue());
            domainKeys.add(key);
            ProductVariationEntity existingEntity = existingByKey.get(key);

            if (existingEntity != null) {
                existingEntity.setVariantOptionValue(variation.getOptionName());
                existingEntity.setVariantTypeValue(variation.getTypeName());
            } else {
                ProductVariationEntity newEntity = toProductVariationEntity(variation);
                variantEntity.addProductVariation(newEntity);
            }
        }

        variantEntity.getProductVariations().removeIf(v ->
                !domainKeys.contains(variationKey(v.getId().getVariantOptionUuid(), v.getId().getVariantTypeUuid()))
        );
    }

    private String variationKey(String optionId, String typeId) {
        return optionId + "::" + typeId;
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

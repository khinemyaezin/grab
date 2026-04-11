package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.entity.entity.MediaEntity;
import com.catalog.infrastructure.entity.entity.ProductDescriptionEntity;
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

    @Override
    public ProductEntity buildFullEntityGraph(Product product, ProductEntity entity) {
        if (entity == null) {
            entity = new ProductEntity();
        }

        productEntityMapper.toEntity(product, entity);
        mergeVariants(entity, product.getVariants());
        mergeDescriptions(entity, product.getDescriptions());
        mergeMedias(entity, product.getMedias());

        return entity;
    }

    private void mergeDescriptions(ProductEntity entity, List<Description> descriptions) {
        if (descriptions == null) {
            entity.clearDescriptions();
            return;
        }

        Map<String, ProductDescriptionEntity> existingByUuid = entity.getDescriptions().stream()
                .filter(descriptionEntity -> Objects.nonNull(descriptionEntity.getUuid()))
                .collect(Collectors.toMap(
                        ProductDescriptionEntity::getUuid,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<ProductDescriptionEntity> mergedDescriptions = new ArrayList<>();
        for (Description description : descriptions) {
            String descriptionId = description.getId() == null ? null : description.getId().getValue();
            ProductDescriptionEntity descriptionEntity = existingByUuid.get(descriptionId);
            if (descriptionEntity == null) {
                descriptionEntity = new ProductDescriptionEntity();
            }
            descriptionEntity.setUuid(descriptionId);
            descriptionEntity.setName(description.getName());
            descriptionEntity.setTitle(description.getTitle());
            descriptionEntity.setDescription(description.getDescription());
            mergedDescriptions.add(descriptionEntity);
        }

        entity.clearDescriptions();
        mergedDescriptions.forEach(entity::addProductDescription);
    }

    private void mergeMedias(ProductEntity entity, List<ProductMedia> medias) {
        if (medias == null) {
            entity.clearMedias();
            return;
        }

        Map<String, MediaEntity> existingByUuid = entity.getMedias().stream()
                .filter(mediaEntity -> mediaEntity.getUuid() != null)
                .collect(Collectors.toMap(
                        MediaEntity::getUuid,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        Map<String, MediaEntity> existingByPath = entity.getMedias().stream()
                .filter(mediaEntity -> mediaEntity.getPath() != null)
                .collect(Collectors.toMap(
                        MediaEntity::getPath,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<MediaEntity> mergedMedias = new ArrayList<>();
        for (ProductMedia media : medias) {
            String mediaId = media.getId() == null ? null : media.getId().getValue();
            MediaEntity mediaEntity = existingByUuid.get(mediaId);
            if (mediaEntity == null) {
                mediaEntity = existingByPath.get(media.getPath());
            }
            if (mediaEntity == null) {
                mediaEntity = new MediaEntity();
            }
            mediaEntity.setUuid(mediaId);
            mediaEntity.setType(media.getType());
            mediaEntity.setPath(media.getPath());
            mergedMedias.add(mediaEntity);
        }

        entity.clearMedias();
        mergedMedias.forEach(entity::addMedia);
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
                null
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
                ProductVariantEntity productVariantEntity = toProductVariantEntity(variantDomain);
                for (ProductVariation variation : variantDomain.getVariations()) {
                    ProductVariationEntity variationEntity = toProductVariationEntity(variation);
                    productVariantEntity.addProductVariation(variationEntity);
                }
                resultVariants.add(productVariantEntity);
            }
        }
        productEntity.getProductVariants()
                .removeIf(e -> !processedUuids.contains(e.getUuid()));

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

            if (existingEntity == null) {
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

    @Override
    public Product toFullDomainGraph(ProductEntity productJpaEntity) {
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

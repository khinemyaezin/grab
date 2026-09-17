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
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
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
        mergeDescriptions(entity, product.getDescriptions());
        mergeMedias(entity, product.getMedias());
        mergeVariants(entity, product.getVariants());

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
        Map<String, MediaEntity> existingByStorageKey = entity.getMedias().stream()
                .map(mediaEntity -> mediaEntity.getStorageKey() != null ? mediaEntity.getStorageKey() : mediaEntity.getPath())
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        key -> key,
                        key -> entity.getMedias().stream()
                                .filter(media -> key.equals(media.getStorageKey()) || key.equals(media.getPath()))
                                .findFirst()
                                .orElseThrow(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<MediaEntity> mergedMedias = new ArrayList<>();
        int fallbackRank = 0;
        for (ProductMedia media : medias) {
            String mediaId = media.getId() == null ? null : media.getId().getValue();
            MediaEntity mediaEntity = existingByUuid.get(mediaId);
            if (mediaEntity == null) {
                mediaEntity = existingByStorageKey.get(media.getStorageKey());
            }
            if (mediaEntity == null) {
                mediaEntity = existingByPath.get(media.getStorageKey());
            }
            if (mediaEntity == null) {
                mediaEntity = new MediaEntity();
            }
            mediaEntity.setUuid(mediaId);
            mediaEntity.setType(media.getContentType());
            mediaEntity.setContentType(media.getContentType());
            mediaEntity.setStorageKey(media.getStorageKey());
            mediaEntity.setPath(media.getStorageKey());
            mediaEntity.setUrl(media.getUrl());
            mediaEntity.setRank(media.getRank() >= 0 ? media.getRank() : fallbackRank);
            mergedMedias.add(mediaEntity);
            fallbackRank++;
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
                mergeVariantMedia(productEntity, variantEntity, variantDomain);
                processedUuids.add(uuid);
                resultVariants.add(variantEntity);
            } else {
                ProductVariantEntity productVariantEntity = toProductVariantEntity(variantDomain);
                for (ProductVariation variation : variantDomain.getVariations()) {
                    ProductVariationEntity variationEntity = toProductVariationEntity(variation);
                    productVariantEntity.addProductVariation(variationEntity);
                }
                mergeVariantMedia(productEntity, productVariantEntity, variantDomain);
                resultVariants.add(productVariantEntity);
            }
        }
        productEntity.getProductVariants().stream()
                .filter(e -> !processedUuids.contains(e.getUuid()))
                .forEach(ProductVariantEntity::clearMedias);
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

    private void mergeVariantMedia(
            ProductEntity productEntity,
            ProductVariantEntity variantEntity,
            ProductVariant variant
    ) {
        Map<String, MediaEntity> productMediaByUuid = productEntity.getMedias().stream()
                .filter(media -> media.getUuid() != null)
                .collect(Collectors.toMap(MediaEntity::getUuid, Function.identity(), (left, right) -> left));

        variantEntity.clearMedias();
        for (Id mediaId : variant.getMediaIds()) {
            MediaEntity mediaEntity = productMediaByUuid.get(mediaId.getValue());
            if (mediaEntity != null) {
                variantEntity.addMedia(mediaEntity);
            }
        }
        variantEntity.setThumbnailMediaUuid(
                variant.getThumbnailMediaId() == null ? null : variant.getThumbnailMediaId().getValue()
        );
    }

    private String variationKey(String optionId, String typeId) {
        return optionId + "::" + typeId;
    }

    @Override
    public Product toFullDomainGraph(ProductEntity productJpaEntity) {
        List<ProductMedia> productMedias = toProductMedias(productJpaEntity.getMedias());
        Map<String, Id> mediaIdByUuid = productMedias.stream()
                .filter(media -> media.getId() != null)
                .collect(Collectors.toMap(media -> media.getId().getValue(), ProductMedia::getId, (left, right) -> left));

        List<ProductVariant> productVariants = new ArrayList<>();

        for(ProductVariantEntity variantEntity : productJpaEntity.getProductVariants()) {
            List<ProductVariation> productVariations = new ArrayList<>();

            for( ProductVariationEntity variationEntity: variantEntity.getProductVariations()) {
                ProductVariation variation = productVariationMapper.toDomain(variationEntity);
                productVariations.add(variation);
            }

            List<Id> mediaIds = variantMediaIds(variantEntity, mediaIdByUuid);
            Id thumbnail = variantThumbnail(variantEntity, mediaIdByUuid);
            productVariants.add(productVariantMapper.toDomain(variantEntity, productVariations, mediaIds, thumbnail));
        }

        return productMapper.toDomain(productJpaEntity, productVariants, productMedias);
    }

    private List<ProductMedia> toProductMedias(List<MediaEntity> mediaEntities) {
        if (mediaEntities == null || mediaEntities.isEmpty()) {
            return List.of();
        }
        List<ProductMedia> medias = new ArrayList<>();
        int index = 0;
        for (MediaEntity mediaEntity : mediaEntities) {
            String domainId = mediaEntity.getUuid() != null
                    ? mediaEntity.getUuid()
                    : (mediaEntity.getId() == null ? null : String.valueOf(mediaEntity.getId()));
            String storageKey = firstNonBlank(mediaEntity.getStorageKey(), mediaEntity.getPath());
            String url = firstNonBlank(mediaEntity.getUrl(), storageKey);
            String contentType = firstNonBlank(mediaEntity.getContentType(), mediaEntity.getType());
            
            int rank = mediaEntity.getRank() >= 0 ? mediaEntity.getRank() : index;
            
            medias.add(new ProductMedia(
                    domainId == null ? null : new CommonId(domainId),
                    storageKey,
                    url,
                    contentType,
                    rank
            ));
            index++;
        }
        // FIX: Ensure domain list is sorted by rank ascending
        medias.sort(Comparator.comparingInt(ProductMedia::getRank));
        return medias;
    }


    private List<Id> variantMediaIds(ProductVariantEntity variantEntity, Map<String, Id> mediaIdByUuid) {
        List<Id> mediaIds = new ArrayList<>();
        for (MediaEntity mediaEntity : variantEntity.getMedias()) {
            String key = mediaEntity.getUuid() != null
                    ? mediaEntity.getUuid()
                    : (mediaEntity.getId() == null ? null : String.valueOf(mediaEntity.getId()));
            Id mediaId = key == null ? null : mediaIdByUuid.getOrDefault(key, new CommonId(key));
            if (mediaId != null) {
                mediaIds.add(mediaId);
            }
        }
        return mediaIds;
    }

    private Id variantThumbnail(ProductVariantEntity variantEntity, Map<String, Id> mediaIdByUuid) {
        if (variantEntity.getThumbnailMediaUuid() == null) {
            return null;
        }
        return mediaIdByUuid.getOrDefault(
                variantEntity.getThumbnailMediaUuid(),
                new CommonId(variantEntity.getThumbnailMediaUuid())
        );
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

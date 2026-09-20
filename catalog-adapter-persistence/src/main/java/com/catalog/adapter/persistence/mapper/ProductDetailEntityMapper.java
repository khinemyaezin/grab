package com.catalog.adapter.persistence.mapper;

import com.catalog.adapter.persistence.entity.MediaEntity;
import com.catalog.adapter.persistence.entity.ProductDescriptionEntity;
import com.catalog.adapter.persistence.entity.ProductEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.entity.ProductVariationEntity;
import com.catalog.application.model.read.ProductDetailView;

public final class ProductDetailEntityMapper {
    private ProductDetailEntityMapper() {
    }

    public static ProductDetailView toDetailView(ProductEntity entity) {
        return new ProductDetailView(
                entity.getUuid(),
                entity.getName(),
                entity.getMerchantId(),
                entity.getStatus(),
                entity.getSlug(),
                entity.getListingCondition(),
                entity.getCategoryId(),
                entity.isFeatured(),
                entity.getDescriptions().stream().map(ProductDetailEntityMapper::toDescription).toList(),
                entity.getMedias().stream().map(ProductDetailEntityMapper::toMedia).toList(),
                entity.getProductVariants().stream().map(ProductDetailEntityMapper::toVariant).toList()
        );
    }

    public static ProductDetailView.VariantView toVariant(ProductVariantEntity variant) {
        return new ProductDetailView.VariantView(
                variant.getUuid(),
                variant.getSku(),
                variant.getStatus(),
                variant.isManageInventory(),
                variant.getMedias().stream().map(MediaEntity::getUuid).toList(),
                variant.getThumbnailMediaUuid(),
                variant.getProductVariations().stream().map(ProductDetailEntityMapper::toVariation).toList()
        );
    }

    private static ProductDetailView.DescriptionView toDescription(ProductDescriptionEntity entity) {
        return new ProductDetailView.DescriptionView(
                entity.getUuid(),
                entity.getName(),
                entity.getTitle(),
                entity.getDescription()
        );
    }

    private static ProductDetailView.MediaView toMedia(MediaEntity entity) {
        return new ProductDetailView.MediaView(
                entity.getUuid(),
                entity.getStorageKey() != null ? entity.getStorageKey() : entity.getPath(),
                entity.getUrl(),
                entity.getContentType() != null ? entity.getContentType() : entity.getType(),
                entity.getRank()
        );
    }

    private static ProductDetailView.VariationView toVariation(ProductVariationEntity entity) {
        return new ProductDetailView.VariationView(
                entity.getId().getVariantOptionUuid(),
                entity.getId().getVariantTypeUuid()
        );
    }
}

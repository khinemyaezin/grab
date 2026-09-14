package com.catalog.domain.service.impl;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.exception.CatalogDomainError;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.service.ProductMediaService;
import com.grab.framework.id.Id;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DefaultProductMediaService implements ProductMediaService {

    @Override
    public void replaceGallery(Product product, List<ProductMedia> medias) {
        Objects.requireNonNull(product, "product");
        product.replaceMedias(normalizeGallery(medias));
    }

    @Override
    public void assignVariantMedia(
            Product product,
            Id variantId,
            Collection<Id> mediaIds,
            Id thumbnailMediaId
    ) {
        Objects.requireNonNull(product, "product");
        Set<Id> owned = ownedMediaIds(product.getMedias());
        List<Id> nextMediaIds = mediaIds == null ? new ArrayList<>() : new ArrayList<>(mediaIds);
        for (Id mediaId : nextMediaIds) {
            ensureMediaOwnedByProduct(variantId, mediaId, owned);
        }
        if (thumbnailMediaId != null) {
            ensureMediaOwnedByProduct(variantId, thumbnailMediaId, owned);
            if (!nextMediaIds.contains(thumbnailMediaId)) {
                nextMediaIds.add(thumbnailMediaId);
            }
        }
        product.applyVariantMedia(variantId, nextMediaIds, thumbnailMediaId);
    }

    private List<ProductMedia> normalizeGallery(List<ProductMedia> medias) {
        if (medias == null || medias.isEmpty()) {
            return List.of();
        }
        Set<String> ids = new HashSet<>();
        Set<String> storageKeys = new HashSet<>();
        List<ProductMedia> normalized = new ArrayList<>(medias.size());
        for (ProductMedia media : medias) {
            Objects.requireNonNull(media, "media");
            if (media.getId() != null) {
                String idValue = media.getId().getValue();
                if (!ids.add(idValue)) {
                    throw duplicateMedia(idValue);
                }
            }
            if (!storageKeys.add(media.getStorageKey())) {
                throw duplicateMedia(media.getStorageKey());
            }
            normalized.add(media);
        }
        return normalized;
    }

    private Set<Id> ownedMediaIds(List<ProductMedia> medias) {
        Set<Id> owned = new HashSet<>();
        for (ProductMedia media : medias) {
            if (media.getId() != null) {
                owned.add(media.getId());
            }
        }
        return owned;
    }

    private void ensureMediaOwnedByProduct(Id variantId, Id mediaId, Set<Id> owned) {
        if (mediaId == null || owned.contains(mediaId)) {
            return;
        }
        throw new CatalogDomainValidationException(
                new CatalogDomainError.VariantMediaNotOwnedByProduct(
                        variantId == null ? null : variantId.getValue(),
                        mediaId.getValue()
                ),
                "Variant media must be a subset of the product media collection."
        );
    }

    private CatalogDomainValidationException duplicateMedia(String identifier) {
        return new CatalogDomainValidationException(
                new CatalogDomainError.DuplicateProductMedia(identifier),
                "Duplicate product media: " + identifier
        );
    }
}

package com.catalog.application.service;

import com.catalog.application.model.read.GetProductBySlugResult;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.domain.aggregate.ProductMedia;
import com.grab.framework.storage.FileStoragePort;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class ProductMediaConverter {

    private final FileStoragePort fileStoragePort;

    public List<GetProductResult.Media> toGetProductMedias(List<ProductMedia> medias) {
        return map(medias, GetProductResult.Media::new);
    }

    public List<GetProductResult.Media> toGetProductMediasFromViews(List<ProductDetailView.MediaView> medias) {
        if (medias == null || medias.isEmpty()) {
            return List.of();
        }
        return medias.stream()
                .sorted(java.util.Comparator.comparingInt(ProductDetailView.MediaView::rank))
                .map(media -> new GetProductResult.Media(
                        media.id(),
                        media.storageKey(),
                        media.url() != null ? media.url() : fileStoragePort.resolvePublicUrl(media.storageKey()),
                        media.contentType(),
                        media.rank()
                ))
                .toList();
    }

    public List<GetProductBySlugResult.Media> toGetProductBySlugMediasFromViews(List<ProductDetailView.MediaView> medias) {
        if (medias == null || medias.isEmpty()) {
            return List.of();
        }
        return medias.stream()
                .sorted(java.util.Comparator.comparingInt(ProductDetailView.MediaView::rank))
                .map(media -> new GetProductBySlugResult.Media(
                        media.id(),
                        media.storageKey(),
                        media.url() != null ? media.url() : fileStoragePort.resolvePublicUrl(media.storageKey()),
                        media.contentType(),
                        media.rank()
                ))
                .toList();
    }

    public List<GetProductBySlugResult.Media> toGetProductBySlugMedias(List<ProductMedia> medias) {
        return map(medias, GetProductBySlugResult.Media::new);
    }

    @FunctionalInterface
    private interface MediaFactory<T> {
        T create(String id, String storageKey, String url, String contentType, int rank);
    }

    private <T> List<T> map(List<ProductMedia> medias, MediaFactory<T> factory) {
        if (medias == null || medias.isEmpty()) {
            return List.of();
        }
        return medias.stream()
                .sorted(Comparator.comparingInt(ProductMedia::getRank))
                .map(media -> factory.create(
                        media.getId() == null ? null : media.getId().getValue(),
                        media.getStorageKey(),
                        fileStoragePort.resolvePublicUrl(media.getStorageKey()),
                        media.getContentType(),
                        media.getRank()
                ))
                .toList();
    }
}

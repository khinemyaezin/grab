package com.grab.store.catalog.internal.query;

import com.catalog.domain.aggregate.ProductMedia;
import com.grab.framework.storage.FileStoragePort;
import com.grab.store.catalog.queries.GetProductBySlugResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMediaQueryMapper {

    private final FileStoragePort fileStoragePort;

    public List<GetProductResult.Media> toGetProductMedias(List<ProductMedia> medias) {
        return map(medias, GetProductResult.Media::new);
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

package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.ProductMediaService;
import com.catalog.domain.valueobject.ProductMediaKey;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.storage.FileStoragePort;
import com.grab.store.catalog.internal.command.ProductMediaResult;
import com.grab.store.catalog.internal.command.ReplaceProductMediaCommand;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReplaceProductMediaCommandHandler implements CommandHandler<ReplaceProductMediaCommand, ProductMediaResult> {

    private static final Logger log = Loggers.getLogger(ReplaceProductMediaCommandHandler.class);

    private final ProductRepository productRepository;
    private final FileStoragePort fileStoragePort;
    private final IdGenerator idGenerator;
    private final ProductMediaService productMediaService;

    @Override
    @CatalogTransactional
    public ProductMediaResult handle(ReplaceProductMediaCommand command) {
        Product product = loadProduct(command.productId(), command.merchantId());
        List<ProductMedia> medias = new ArrayList<>();
        if (command.medias() != null) {
            int index = 0;
            for (ReplaceProductMediaCommand.Media media : command.medias()) {
                ProductMediaKey storageKey = parseStorageKey(media.storageKey());
                ProductMediaKey persistedKey = persistableStorageKey(
                        storageKey,
                        command.merchantId(),
                        command.productId()
                );
                int rank = media.rank() == null ? index : media.rank();
                medias.add(new ProductMedia(
                        media.id() == null ? idGenerator.generateId() : media.id(),
                        persistedKey.value(),
                        fileStoragePort.resolvePublicUrl(persistedKey.value()),
                        media.contentType(),
                        rank
                ));
                index++;
            }
        }

        productMediaService.replaceGallery(product, medias);
        productRepository.save(product);

        return new ProductMediaResult(product.getId().getValue(), mapMedias(product.getMedias()));
    }

    @Override
    public Class<ReplaceProductMediaCommand> getCommandType() {
        return ReplaceProductMediaCommand.class;
    }

    private ProductMediaKey persistableStorageKey(ProductMediaKey storageKey, Id merchantId, Id productId) {
        if (storageKey.isForeignStaged(merchantId)) {
            throw new CatalogServiceException(
                    new CatalogServiceError.InvalidProductMediaPatch("storageKey is outside this merchant")
            );
        }
        if (!fileStoragePort.objectExists(storageKey.value())) {
            throw new CatalogServiceException(new CatalogServiceError.MediaObjectNotFound(storageKey.value()));
        }
        if (!(storageKey.isStaged() && storageKey.isOwnedBy(merchantId))) {
            return storageKey;
        }

        ProductMediaKey destKey = storageKey.promoteToProduct(productId);
        fileStoragePort.copy(storageKey.value(), destKey.value());
        deleteStagedBestEffort(storageKey.value());
        return destKey;
    }

    private void deleteStagedBestEffort(String stagedKey) {
        try {
            fileStoragePort.delete(stagedKey);
        } catch (RuntimeException exception) {
            log.warn("Failed to delete staged media object {}", stagedKey, exception);
        }
    }

    private Product loadProduct(Id productId, Id merchantId) {
        return productRepository.find(productId, merchantId).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(productId.getValue()))
        );
    }

    private List<ProductMediaResult.Media> mapMedias(List<ProductMedia> medias) {
        return medias.stream()
                .map(media -> new ProductMediaResult.Media(
                        media.getId() == null ? null : new CommonId(media.getId().getValue()),
                        media.getStorageKey(),
                        media.getUrl(),
                        media.getContentType(),
                        media.getRank()
                ))
                .toList();
    }

    private ProductMediaKey parseStorageKey(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.InvalidProductMediaPatch("storageKey is required")
            );
        }
        try {
            return new ProductMediaKey(storageKey);
        } catch (IllegalArgumentException exception) {
            throw new CatalogServiceException(
                    new CatalogServiceError.InvalidProductMediaPatch(exception.getMessage())
            );
        }
    }
}

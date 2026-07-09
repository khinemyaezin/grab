package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.GetProductPayload;
import com.grab.store.catalog.internal.command.ProductMediaResult;
import com.grab.store.catalog.internal.command.ReplaceProductMediaCommand;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReplaceProductMediaCommandHandler implements CommandHandler<ReplaceProductMediaCommand, ProductMediaResult> {

    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public ProductMediaResult handle(ReplaceProductMediaCommand command) {
        Product product = loadProduct(command.productId(), command.merchantId());
        List<ProductMedia> medias = command.medias() == null
                ? List.of()
                : command.medias().stream()
                .map(media -> {
                    validateRequired(media.path(), "path");
                    return new ProductMedia(media.id(), media.type(), media.path());
                })
                .toList();

        product.replaceMedias(medias);
        productRepository.save(product);

        return new ProductMediaResult(product.getId().getValue(), mapMedias(product.getMedias()));
    }

    @Override
    public Class<ReplaceProductMediaCommand> getCommandType() {
        return ReplaceProductMediaCommand.class;
    }

    private Product loadProduct(com.grab.framework.id.Id productId, com.grab.framework.id.Id merchantId) {
        return productRepository.find(productId, merchantId).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(productId.getValue()))
        );
    }

    private List<GetProductPayload.Media> mapMedias(List<ProductMedia> medias) {
        return medias.stream()
                .map(media -> new GetProductPayload.Media(
                        media.getId() == null ? null : new CommonId(media.getId().getValue()),
                        media.getType(),
                        media.getPath()
                ))
                .toList();
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.InvalidProductMediaPatch(fieldName + " is required")
            );
        }
    }
}

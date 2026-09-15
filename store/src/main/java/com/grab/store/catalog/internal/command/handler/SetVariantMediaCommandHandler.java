package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.service.ProductMediaService;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.command.SetVariantMediaCommand;
import com.grab.store.catalog.internal.command.SetVariantMediaResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetVariantMediaCommandHandler implements CommandHandler<SetVariantMediaCommand, SetVariantMediaResult> {

    private final ProductRepository productRepository;
    private final ProductMediaService productMediaService;

    @Override
    @CatalogTransactional
    public SetVariantMediaResult handle(SetVariantMediaCommand command) {
        Product product = productRepository.find(command.productId(), command.merchantId()).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(command.productId().getValue()))
        );

        productMediaService.assignVariantMedia(product, command.variantId(), command.mediaIds(), command.thumbnailMediaId());
        productRepository.save(product);

        ProductVariant variant = product.findVariantById(command.variantId()).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.VariantNotFound(command.variantId().getValue()))
        );
        return new SetVariantMediaResult(
                product.getId().getValue(),
                variant.getId().getValue(),
                variant.getMediaIds().stream().map(Id::getValue).toList(),
                variant.getThumbnailMediaId() == null ? null : variant.getThumbnailMediaId().getValue()
        );
    }

    @Override
    public Class<SetVariantMediaCommand> getCommandType() {
        return SetVariantMediaCommand.class;
    }
}

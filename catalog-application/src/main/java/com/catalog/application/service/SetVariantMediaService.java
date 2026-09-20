package com.catalog.application.service;

import com.catalog.application.port.inbound.SetVariantMediaUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.service.ProductMediaService;
import com.grab.framework.id.Id;
import com.catalog.application.command.SetVariantMediaCommand;
import com.catalog.application.command.SetVariantMediaResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class SetVariantMediaService implements SetVariantMediaUseCase {

    private final ProductRepository productRepository;
    private final ProductMediaService productMediaService;

        public SetVariantMediaResult execute(SetVariantMediaCommand command) {
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
}

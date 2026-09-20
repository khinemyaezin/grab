package com.catalog.application.service;

import com.catalog.application.port.inbound.PublishProductToChannelUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.policy.ProductPublicationPolicy;
import com.catalog.domain.port.outbound.ProductPublicationRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.application.command.PublishProductToChannelCommand;
import com.catalog.application.command.PublishProductToChannelResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@lombok.RequiredArgsConstructor
public class PublishProductToChannelService implements PublishProductToChannelUseCase {

    private final ProductRepository productRepository;
    private final ProductPublicationRepository productPublicationRepository;
    private final ProductPublicationPolicy productPublicationPolicy = new ProductPublicationPolicy();

        public PublishProductToChannelResult execute(PublishProductToChannelCommand command) {
        Product product = productRepository.find(command.productId(), command.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(command.productId().getValue())
                ));
        requireVariantOnProduct(product, command);
        if (productPublicationRepository.exists(command.variantId(), command.salesChannelId())) {
            return new PublishProductToChannelResult(
                    command.productId().getValue(),
                    command.variantId().getValue(),
                    command.salesChannelId().getValue(),
                    false
            );
        }
        if (!productPublicationPolicy.isPublishable(product)) {
            return new PublishProductToChannelResult(
                    command.productId().getValue(),
                    command.variantId().getValue(),
                    command.salesChannelId().getValue(),
                    false
            );
        }
        ProductPublication publication = ProductPublication.publish(
                command.variantId(),
                command.salesChannelId(),
                Instant.now()
        );
        productPublicationRepository.save(publication);
        return new PublishProductToChannelResult(
                command.productId().getValue(),
                command.variantId().getValue(),
                command.salesChannelId().getValue(),
                true
        );
    }

    private void requireVariantOnProduct(Product product, PublishProductToChannelCommand command) {
        boolean variantOnProduct = product.findVariantById(command.variantId()).isPresent();
        if (!variantOnProduct) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantNotFound(command.variantId().getValue())
            );
        }
    }
}

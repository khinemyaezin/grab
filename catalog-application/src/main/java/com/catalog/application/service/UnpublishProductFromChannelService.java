package com.catalog.application.service;

import com.catalog.application.port.inbound.UnpublishProductFromChannelUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.port.outbound.ProductPublicationRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.application.model.write.UnpublishProductFromChannelCommand;
import com.catalog.application.model.write.UnpublishProductFromChannelResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

import java.util.Optional;

@lombok.RequiredArgsConstructor
public class UnpublishProductFromChannelService implements UnpublishProductFromChannelUseCase {

    private final ProductRepository productRepository;
    private final ProductPublicationRepository productPublicationRepository;

        public UnpublishProductFromChannelResult execute(UnpublishProductFromChannelCommand command) {
        Product product = productRepository.find(command.productId(), command.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(command.productId().getValue())
                ));
        requireVariantOnProduct(product, command);
        Optional<ProductPublication> existing = productPublicationRepository.find(
                command.variantId(),
                command.salesChannelId()
        );
        if (existing.isEmpty()) {
            return new UnpublishProductFromChannelResult(
                    command.productId().getValue(),
                    command.variantId().getValue(),
                    command.salesChannelId().getValue(),
                    false
            );
        }
        ProductPublication publication = existing.get();
        publication.unpublish();
        productPublicationRepository.delete(publication);
        return new UnpublishProductFromChannelResult(
                command.productId().getValue(),
                command.variantId().getValue(),
                command.salesChannelId().getValue(),
                true
        );
    }

    private void requireVariantOnProduct(Product product, UnpublishProductFromChannelCommand command) {
        boolean variantOnProduct = product.findVariantById(command.variantId()).isPresent();
        if (!variantOnProduct) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantNotFound(command.variantId().getValue())
            );
        }
    }
}

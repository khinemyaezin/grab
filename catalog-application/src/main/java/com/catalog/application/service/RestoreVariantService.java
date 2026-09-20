package com.catalog.application.service;

import com.catalog.application.port.inbound.RestoreVariantUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.application.model.write.RestoreVariantCommand;
import com.catalog.application.model.write.RestoreVariantResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;

import java.util.Optional;

@lombok.RequiredArgsConstructor
public class RestoreVariantService implements RestoreVariantUseCase {

    private static final Logger log = Loggers.getLogger(RestoreVariantService.class);

    private final ProductRepository productRepository;

        public RestoreVariantResult execute(RestoreVariantCommand command) {
        log.debug("Handling RestoreVariantCommand for productId={}, sku={}", command.productId(), command.variantId());

        Optional<Product> hasProduct = productRepository.find(command.productId(), command.merchantId());
        if (hasProduct.isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFound(command.productId().getValue())
            );
        }

        Product product = hasProduct.get();

        boolean restored = product.restoreVariant(command.variantId());
        if (!restored) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantNotFoundOrNotDeleted(command.variantId().getValue())
            );
        }

        productRepository.save(product);

        String variantStatus = product.findVariantById(command.variantId())
                .map(v -> v.getStatus().name())
                .orElse(null);

        return new RestoreVariantResult(product.getId().getValue(), command.variantId().getValue(), variantStatus);
    }
}

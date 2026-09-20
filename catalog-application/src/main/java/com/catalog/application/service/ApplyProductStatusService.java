package com.catalog.application.service;

import com.catalog.application.port.inbound.ApplyProductStatusUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.command.ApplyProductStatusCommand;
import com.catalog.application.command.ApplyProductStatusResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class ApplyProductStatusService implements ApplyProductStatusUseCase {

    private static final Logger log = Loggers.getLogger(ApplyProductStatusService.class);

    private final ProductRepository productRepository;

        public ApplyProductStatusResult execute(ApplyProductStatusCommand command) {
        Product product = productRepository.find(command.productId(), command.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(command.productId().getValue())
                ));
        applyStatus(product, command.status());
        productRepository.save(product);
        return new ApplyProductStatusResult(
                product.getId().getValue(),
                product.getStatus() == null ? null : product.getStatus().name()
        );
    }

    private void applyStatus(Product product, String status) {
        if (!hasText(status)) {
            return;
        }
        ProductStatus next = ProductStatus.valueOf(status);
        if (product.getStatus() == next) {
            return;
        }
        log.info("Applying product status {} for product {}", next, product.getId().getValue());
        product.changeStatus(next);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

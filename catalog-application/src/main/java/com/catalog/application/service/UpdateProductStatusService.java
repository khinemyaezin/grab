package com.catalog.application.service;

import com.catalog.application.port.inbound.UpdateProductStatusUseCase;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.model.write.UpdateProductStatusCommand;
import com.catalog.application.model.write.UpdateProductStatusResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

import java.util.Optional;

@lombok.RequiredArgsConstructor
public class UpdateProductStatusService implements UpdateProductStatusUseCase {

    private static final Logger log = Loggers.getLogger(UpdateProductStatusService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

        public UpdateProductStatusResult execute(UpdateProductStatusCommand command) {
        log.debug("Handling UpdateProductStatusCommand for productId={}, status={}", command.productId(), command.status());

        Optional<Product> hasProduct = productRepository.find(command.productId(), command.merchantId());
        if (hasProduct.isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFound(command.productId().getValue())
            );
        }

        Product product = hasProduct.get();
        Category category = categoryRepository.find(product.getCategoryId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(product.getCategoryId().getValue())
                ));

        String oldStatus = product.getStatus() == null ? null : product.getStatus().name();

        ProductStatus newStatus = ProductStatus.valueOf(command.status());
        if (newStatus != ProductStatus.ARCHIVED) {
            CatalogPolicyValidator.validateCategoryPolicy(category);
        }
        product.changeStatus(newStatus);

        productRepository.save(product);

        String newStatusName = product.getStatus() == null ? null : product.getStatus().name();

        return new UpdateProductStatusResult(product.getId().getValue(), oldStatus, newStatusName);
    }
}

package com.catalog.application.service;

import com.catalog.application.port.inbound.ModerateProductUseCase;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.application.command.ModerateProductCommand;
import com.catalog.application.command.ModerateProductResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.service.CatalogPolicyValidator;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class ModerateProductService implements ModerateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

        public ModerateProductResult execute(ModerateProductCommand command) {
        Product product = productRepository.find(command.productId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(command.productId().getValue())
                ));

        Category category = categoryRepository.find(product.getCategoryId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(product.getCategoryId().getValue())
                ));

        String oldStatus = product.getStatus().name();
        Action action = Action.valueOf(command.action());

        switch (action) {
            case PUBLISH -> {
                CatalogPolicyValidator.validateCategoryPolicy(category);
                product.publish();
            }
            case SUSPEND -> product.suspend(command.reason());
            case RESTORE -> {
                CatalogPolicyValidator.validateCategoryPolicy(category);
                product.restore();
            }
        }

        productRepository.save(product);
        return new ModerateProductResult(
                product.getId().getValue(),
                action.name(),
                oldStatus,
                product.getStatus().name(),
                command.reason()
        );
    }

    private enum Action {
        PUBLISH,
        SUSPEND,
        RESTORE
    }
}

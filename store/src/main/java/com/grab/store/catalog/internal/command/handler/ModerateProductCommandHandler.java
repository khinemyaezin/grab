package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.ModerateProductCommand;
import com.grab.store.catalog.internal.command.ModerateProductResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.CatalogPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModerateProductCommandHandler implements CommandHandler<ModerateProductCommand, ModerateProductResult> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @CatalogTransactional
    public ModerateProductResult handle(ModerateProductCommand command) {
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
            case SUBMIT_REVIEW -> {
                CatalogPolicyValidator.validateCategoryPolicy(category);
                product.submitForReview();
            }
            case APPROVE -> {
                CatalogPolicyValidator.validateActivationPolicy(category, product);
                product.approve();
            }
            case REJECT -> product.reject(command.reason());
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

    @Override
    public Class<ModerateProductCommand> getCommandType() {
        return ModerateProductCommand.class;
    }

    private enum Action {
        SUBMIT_REVIEW,
        APPROVE,
        REJECT,
        SUSPEND,
        RESTORE
    }
}

package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.UpdateProductStatusCommand;
import com.grab.store.catalog.internal.command.UpdateProductStatusResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.CatalogPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateProductStatusCommandHandler implements CommandHandler<UpdateProductStatusCommand, UpdateProductStatusResult> {

    private static final Logger log = Loggers.getLogger(UpdateProductStatusCommandHandler.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @CatalogTransactional
    public UpdateProductStatusResult handle(UpdateProductStatusCommand command) {
        log.debug("Handling UpdateProductStatusCommand for productId={}, status={}", command.productId(), command.status());

        Optional<Product> hasProduct = productRepository.find(command.productId());
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
        if (newStatus == ProductStatus.ACTIVE) {
            CatalogPolicyValidator.validateActivationPolicy(category, product);
        }
        if (newStatus == ProductStatus.IN_REVIEW || newStatus == ProductStatus.DRAFT || newStatus == ProductStatus.SUSPENDED) {
            CatalogPolicyValidator.validateCategoryPolicy(category, product);
        }
        product.changeStatus(newStatus);

        productRepository.save(product);

        String newStatusName = product.getStatus() == null ? null : product.getStatus().name();

        return new UpdateProductStatusResult(product.getId().getValue(), oldStatus, newStatusName);
    }

    @Override
    public Class<UpdateProductStatusCommand> getCommandType() {
        return UpdateProductStatusCommand.class;
    }
}

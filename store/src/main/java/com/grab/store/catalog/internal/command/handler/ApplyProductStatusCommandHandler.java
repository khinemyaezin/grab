package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.ApplyProductStatusCommand;
import com.grab.store.catalog.internal.command.ApplyProductStatusResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ApplyProductStatusCommandHandler implements CommandHandler<ApplyProductStatusCommand, ApplyProductStatusResult> {

    private static final Logger log = Loggers.getLogger(ApplyProductStatusCommandHandler.class);

    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public ApplyProductStatusResult handle(ApplyProductStatusCommand command) {
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

    @Override
    public Class<ApplyProductStatusCommand> getCommandType() {
        return ApplyProductStatusCommand.class;
    }

    private void applyStatus(Product product, String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        ProductStatus next = ProductStatus.valueOf(status);
        if (product.getStatus() == next) {
            return;
        }
        log.info("Applying product status {} for product {}", next, product.getId().getValue());
        product.changeStatus(next);
    }
}

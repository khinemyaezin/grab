package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.DeleteVariantCommand;
import com.grab.store.catalog.internal.command.DeleteVariantResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DeleteVariantCommandHandler implements CommandHandler<DeleteVariantCommand, DeleteVariantResult> {

    private static final Logger log = Loggers.getLogger(DeleteVariantCommandHandler.class);

    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public DeleteVariantResult handle(DeleteVariantCommand command) {
        log.debug("Handling DeleteVariantCommand for productId={}, variantId={}", command.productId(), command.variantId());

        Optional<Product> hasProduct = productRepository.find(command.productId());
        if (hasProduct.isEmpty()) {
            log.warn("Product not found for delete variant: {}", command.productId());
            return new DeleteVariantResult(command.productId().getValue(), command.variantId().getValue(), false);
        }

        Product product = hasProduct.get();

        product.applySoftDeleteVariants(Set.of(command.variantId()));

        productRepository.save(product);

        boolean deleted = product.findVariantById(command.variantId()).map(ProductVariant::isDeleted).orElse(false);

        return new DeleteVariantResult(product.getId().getValue(), command.variantId().getValue(), deleted);
    }

    @Override
    public Class<DeleteVariantCommand> getCommandType() {
        return DeleteVariantCommand.class;
    }
}

package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.grab.store.catalog.internal.command.DeleteProductCommand;
import com.grab.store.catalog.internal.command.DeleteProductResult;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeleteProductCommandHandler implements CommandHandler<DeleteProductCommand, DeleteProductResult> {

    private static final Logger log = Loggers.getLogger(DeleteProductCommandHandler.class);

    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public DeleteProductResult handle(DeleteProductCommand command) {
        log.debug("Handling DeleteProductCommand for product: {}", command.productId());

        Optional<Product> product = productRepository.find(command.productId());
        if (product.isEmpty()) {
            log.warn("Product not found for deletion: {}", command.productId());
            return new DeleteProductResult(false);
        }

        Product existingProduct = product.get();
        existingProduct.delete();
        productRepository.save(existingProduct);

        log.info("Product deleted successfully: {}", command.productId());

        return new DeleteProductResult(true);
    }

    @Override
    public Class<DeleteProductCommand> getCommandType() {
        return DeleteProductCommand.class;
    }
}

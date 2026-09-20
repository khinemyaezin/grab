package com.catalog.application.service;

import com.catalog.application.port.inbound.DeleteProductUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.application.model.write.DeleteProductCommand;
import com.catalog.application.model.write.DeleteProductResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;

import java.util.Optional;

@lombok.RequiredArgsConstructor
public class DeleteProductService implements DeleteProductUseCase {

    private static final Logger log = Loggers.getLogger(DeleteProductService.class);

    private final ProductRepository productRepository;

        public DeleteProductResult execute(DeleteProductCommand command) {
        log.debug("Handling DeleteProductCommand for product: {}", command.productId());

        Optional<Product> product = productRepository.find(command.productId(), command.merchantId());
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
}

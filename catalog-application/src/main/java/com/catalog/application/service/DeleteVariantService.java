package com.catalog.application.service;

import com.catalog.application.port.inbound.DeleteVariantUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.application.model.write.DeleteVariantCommand;
import com.catalog.application.model.write.DeleteVariantResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;

import java.util.Optional;

@lombok.RequiredArgsConstructor
public class DeleteVariantService implements DeleteVariantUseCase {

    private static final Logger log = Loggers.getLogger(DeleteVariantService.class);

    private final ProductRepository productRepository;

        public DeleteVariantResult execute(DeleteVariantCommand command) {
        log.debug("Handling DeleteVariantCommand for productId={}, sku={}", command.productId(), command.variantId());

        Optional<Product> hasProduct = productRepository.find(command.productId(), command.merchantId());
        if (hasProduct.isEmpty()) {
            log.warn("Product not found for delete variant: {}", command.productId());
            return new DeleteVariantResult(command.productId().getValue(), command.variantId().getValue(), false);
        }

        Product product = hasProduct.get();

        product.removeVariant(command.variantId());

        productRepository.save(product);

        return new DeleteVariantResult(product.getId().getValue(), command.variantId().getValue(), true);
    }
}

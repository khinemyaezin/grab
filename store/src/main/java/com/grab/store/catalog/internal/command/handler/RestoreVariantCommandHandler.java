package com.grab.store.catalog.internal.command.handler;

import com.grab.store.catalog.internal.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.RestoreVariantCommand;
import com.grab.store.catalog.internal.command.RestoreVariantResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestoreVariantCommandHandler implements CommandHandler<RestoreVariantCommand, RestoreVariantResult> {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public RestoreVariantResult handle(RestoreVariantCommand command) {
        log.debug("Handling RestoreVariantCommand for productId={}, variantId={}", command.productId(), command.variantId());

        Optional<Product> hasProduct = productRepository.find(command.productId());
        if (hasProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + command.productId());
        }

        Product product = hasProduct.get();

        boolean restored = product.restoreVariant(command.variantId());
        if (!restored) {
            throw new IllegalArgumentException("Variant not found or not deleted: " + command.variantId());
        }

        productRepository.save(product);

        String variantStatus = product.findVariantById(command.variantId())
                .map(v -> v.getStatus().name())
                .orElse(null);

        return new RestoreVariantResult(product.getId().getValue(), command.variantId().getValue(), variantStatus);
    }

    @Override
    public Class<RestoreVariantCommand> getCommandType() {
        return RestoreVariantCommand.class;
    }
}

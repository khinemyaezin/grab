package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.RestoreVariantCommand;
import com.grab.store.catalog.internal.command.RestoreVariantResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestoreVariantCommandHandler implements CommandHandler<RestoreVariantCommand, RestoreVariantResult> {

    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public RestoreVariantResult handle(RestoreVariantCommand command) {
        log.debug("Handling RestoreVariantCommand for productId={}, variantId={}", command.productId(), command.variantId());

        Optional<Product> hasProduct = productRepository.find(command.productId());
        if (hasProduct.isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFound(command.productId().getValue())
            );
        }

        Product product = hasProduct.get();

        boolean restored = product.restoreVariant(command.variantId());
        if (!restored) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantNotFoundOrNotDeleted(command.variantId().getValue())
            );
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

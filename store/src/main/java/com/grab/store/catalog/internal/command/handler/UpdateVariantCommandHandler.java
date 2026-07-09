package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateVariantCommandHandler implements CommandHandler<UpdateVariantCommand, UpdateVariantResult> {

    private static final Logger log = Loggers.getLogger(UpdateVariantCommandHandler.class);

    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public UpdateVariantResult handle(UpdateVariantCommand command) {
        log.debug("Handling UpdateVariantCommand for productId={} sku={}", command.productId(), command.variantId());

        Optional<Product> hasProduct = productRepository.find(command.productId(), command.merchantId());
        if (hasProduct.isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFound(command.productId().getValue())
            );
        }

        Product product = hasProduct.get();

        ProductVariant existing = product.findVariantById(command.variantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.VariantNotFound(command.variantId().getValue())
                ));

        if (existing.isDeleted()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantDeletedCannotUpdate(command.variantId().getValue())
            );
        }

        validateSkuAvailability(command.merchantId(), command.sku(), command.variantId().getValue());

        ProductVariant updated = new ProductVariant(
                existing.getId(),
                command.sku(),
                existing.isActive() ? ProductVariantStatus.ACTIVE : ProductVariantStatus.DELETED,
                existing.getVariations().stream().toList()
        );

        boolean ok = product.updateVariant(existing, updated);
        if (!ok) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantUpdateFailed(command.variantId().getValue()),
                    "Failed to update variant: uniqueness or index error"
            );
        }

        productRepository.save(product);

        return new UpdateVariantResult(
                product.getId().getValue(),
                updated.getId().getValue(),
                updated.getSku(),
                updated.isActive() ? ProductVariantStatus.ACTIVE.name() : ProductVariantStatus.DELETED.name()
        );
    }

    @Override
    public Class<UpdateVariantCommand> getCommandType() {
        return UpdateVariantCommand.class;
    }

    private void validateSkuAvailability(Id merchantId, String sku, String excludeVariantUuid) {
        if (productRepository.isSkuTaken(merchantId, sku, excludeVariantUuid)) {
            throw new CatalogServiceException(
                    new CatalogServiceError.SkuAlreadyExists(sku)
            );
        }
    }
}

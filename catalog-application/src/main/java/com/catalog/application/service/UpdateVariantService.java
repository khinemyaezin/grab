package com.catalog.application.service;

import com.catalog.application.port.inbound.UpdateVariantUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.model.write.UpdateVariantCommand;
import com.catalog.application.model.write.UpdateVariantResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

import java.util.Optional;

@lombok.RequiredArgsConstructor
public class UpdateVariantService implements UpdateVariantUseCase {

    private static final Logger log = Loggers.getLogger(UpdateVariantService.class);

    private final ProductRepository productRepository;

        public UpdateVariantResult execute(UpdateVariantCommand command) {
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

        if (!existing.getSku().equalsIgnoreCase(command.sku())) {
            validateSkuAvailability(command.merchantId(), command.sku(), command.variantId().getValue());
        }

        Boolean requestedManageInventory = command.manageInventory();
        boolean manageInventory = requestedManageInventory != null
                ? requestedManageInventory
                : existing.isManageInventory();
        ProductVariantStatus status = existing.isActive()
                ? ProductVariantStatus.ACTIVE
                : ProductVariantStatus.DELETED;
        ProductVariant updated = new ProductVariant(
                existing.getId(),
                command.sku(),
                status,
                existing.getVariations().stream().toList(),
                manageInventory,
                existing.getMediaIds(),
                existing.getThumbnailMediaId()
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

    private void validateSkuAvailability(Id merchantId, String sku, String excludeVariantUuid) {
        if (productRepository.isSkuTaken(merchantId, sku, excludeVariantUuid)) {
            throw new CatalogServiceException(
                    new CatalogServiceError.SkuAlreadyExists(sku)
            );
        }
    }
}

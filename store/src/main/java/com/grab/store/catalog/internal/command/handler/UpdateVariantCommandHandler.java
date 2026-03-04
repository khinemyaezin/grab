package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateVariantCommandHandler implements CommandHandler<UpdateVariantCommand, UpdateVariantResult> {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public UpdateVariantResult handle(UpdateVariantCommand command) {
        log.debug("Handling UpdateVariantCommand for productId={} variantId={}", command.productId(), command.variantId());

        Optional<Product> hasProduct = productRepository.find(command.productId());
        if (hasProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + command.productId());
        }

        Product product = hasProduct.get();

        ProductVariant existing = product.findVariantById(command.variantId())
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + command.variantId()));

        if (existing.isDeleted()) {
            throw new IllegalStateException("Cannot update deleted variant: " + command.variantId());
        }

        ProductVariant updated = new ProductVariant(
                existing.getId(),
                command.sku(),
                existing.isActive() ? ProductVariantStatus.ACTIVE : ProductVariantStatus.DELETED,
                existing.getVariations().stream().toList()
        );

        boolean ok = product.updateVariant(existing, updated);
        if (!ok) {
            throw new IllegalStateException("Failed to update variant: uniqueness or index error");
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
}

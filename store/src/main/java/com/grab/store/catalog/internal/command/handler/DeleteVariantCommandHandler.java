package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.DeleteVariantCommand;
import com.grab.store.catalog.internal.command.DeleteVariantResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteVariantCommandHandler implements CommandHandler<DeleteVariantCommand, DeleteVariantResult> {

    private final ProductRepository productRepository;

    @Override
    @Transactional
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

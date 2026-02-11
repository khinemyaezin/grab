package com.grab.store.catalog.internal.command.handler;

import com.grab.store.catalog.internal.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.UpdateProductStatusCommand;
import com.grab.store.catalog.internal.command.UpdateProductStatusResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProductStatusCommandHandler implements CommandHandler<UpdateProductStatusCommand, UpdateProductStatusResult> {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public UpdateProductStatusResult handle(UpdateProductStatusCommand command) {
        log.debug("Handling UpdateProductStatusCommand for productId={}, status={}", command.productId(), command.status());

        Optional<Product> hasProduct = productRepository.find(command.productId());
        if (hasProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + command.productId());
        }

        Product product = hasProduct.get();

        String oldStatus = product.getStatus() == null ? null : product.getStatus().name();

        ProductStatus newStatus = ProductStatus.valueOf(command.status());
        product.changeStatus(newStatus);

        productRepository.save(product);

        String newStatusName = product.getStatus() == null ? null : product.getStatus().name();

        return new UpdateProductStatusResult(product.getId().getValue(), oldStatus, newStatusName);
    }

    @Override
    public Class<UpdateProductStatusCommand> getCommandType() {
        return UpdateProductStatusCommand.class;
    }
}

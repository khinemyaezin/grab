package com.grab.store.product.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.command.DeleteProductCommand;
import com.grab.store.product.internal.command.DeleteProductResult;
import com.grab.store.product.internal.cqrs.command.CommandHandler;
import com.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteProductCommandHandler implements CommandHandler<DeleteProductCommand, DeleteProductResult> {

    private final IdGenerator idGenerator;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public DeleteProductResult handle(DeleteProductCommand command) {
        log.debug("Handling DeleteProductCommand for product: {}", command.productId());

        Id productId = idGenerator.generateId(command.productId());

        if (!productRepository.exists(productId)) {
            log.warn("Product not found for deletion: {}", command.productId());
            return new DeleteProductResult(false);
        }

        productRepository.delete(productId);

        log.info("Product deleted successfully: {}", command.productId());

        return new DeleteProductResult(true);
    }

    @Override
    public Class<DeleteProductCommand> getCommandType() {
        return DeleteProductCommand.class;
    }
}

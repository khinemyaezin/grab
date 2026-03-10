package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, UpdateProductResult> {

    private final ProductRepository productRepository;
    private final UniqueSlugResolver uniqueSlugResolver;

    @Override
    @CatalogTransactional
    public UpdateProductResult handle(UpdateProductCommand command) {
        log.debug("Handling UpdateProductCommand for productId={}", command.productId());

        Optional<Product> hasProduct = productRepository.find(command.productId());
        if (hasProduct.isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFound(command.productId().getValue())
            );
        }

        Product product = hasProduct.get();

        String slug = uniqueSlugResolver.resolve(command.slug(), command.name(), product.getId().getValue());
        boolean featured = command.featured() != null ? command.featured() : product.isFeatured();
        product.update(
                command.name(),
                command.categoryId(),
                featured,
                slug
        );

        productRepository.save(product);

        return new UpdateProductResult(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                product.getStatus().name(),
                product.getSlug(),
                product.isFeatured()
        );
    }

    @Override
    public Class<UpdateProductCommand> getCommandType() {
        return UpdateProductCommand.class;
    }
}

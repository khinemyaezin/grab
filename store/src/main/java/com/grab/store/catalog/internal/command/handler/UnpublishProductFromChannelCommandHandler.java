package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.repository.ProductPublicationRepository;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.UnpublishProductFromChannelCommand;
import com.grab.store.catalog.internal.command.UnpublishProductFromChannelResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UnpublishProductFromChannelCommandHandler
        implements CommandHandler<UnpublishProductFromChannelCommand, UnpublishProductFromChannelResult> {

    private final ProductRepository productRepository;
    private final ProductPublicationRepository productPublicationRepository;

    @Override
    @CatalogTransactional
    public UnpublishProductFromChannelResult handle(UnpublishProductFromChannelCommand command) {
        Product product = productRepository.find(command.productId(), command.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(command.productId().getValue())
                ));
        requireVariantOnProduct(product, command);
        Optional<ProductPublication> existing = productPublicationRepository.find(
                command.variantId(),
                command.salesChannelId()
        );
        if (existing.isEmpty()) {
            return new UnpublishProductFromChannelResult(
                    command.productId().getValue(),
                    command.variantId().getValue(),
                    command.salesChannelId().getValue(),
                    false
            );
        }
        ProductPublication publication = existing.get();
        publication.unpublish();
        productPublicationRepository.delete(publication);
        return new UnpublishProductFromChannelResult(
                command.productId().getValue(),
                command.variantId().getValue(),
                command.salesChannelId().getValue(),
                true
        );
    }

    @Override
    public Class<UnpublishProductFromChannelCommand> getCommandType() {
        return UnpublishProductFromChannelCommand.class;
    }

    private void requireVariantOnProduct(Product product, UnpublishProductFromChannelCommand command) {
        boolean variantOnProduct = product.findVariantById(command.variantId()).isPresent();
        if (!variantOnProduct) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantNotFound(command.variantId().getValue())
            );
        }
    }
}

package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.domain.policy.ProductPublicationPolicy;
import com.catalog.domain.repository.ProductPublicationRepository;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.PublishProductToChannelCommand;
import com.grab.store.catalog.internal.command.PublishProductToChannelResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PublishProductToChannelCommandHandler
        implements CommandHandler<PublishProductToChannelCommand, PublishProductToChannelResult> {

    private final ProductRepository productRepository;
    private final ProductPublicationRepository productPublicationRepository;
    private final ProductPublicationPolicy productPublicationPolicy = new ProductPublicationPolicy();

    @Override
    @CatalogTransactional
    public PublishProductToChannelResult handle(PublishProductToChannelCommand command) {
        Product product = productRepository.find(command.productId(), command.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(command.productId().getValue())
                ));
        requireVariantOnProduct(product, command);
        if (productPublicationRepository.exists(command.variantId(), command.salesChannelId())) {
            return new PublishProductToChannelResult(
                    command.productId().getValue(),
                    command.variantId().getValue(),
                    command.salesChannelId().getValue(),
                    false
            );
        }
        productPublicationPolicy.requirePublishable(product);
        ProductPublication publication = ProductPublication.publish(
                command.variantId(),
                command.salesChannelId(),
                Instant.now()
        );
        productPublicationRepository.save(publication);
        return new PublishProductToChannelResult(
                command.productId().getValue(),
                command.variantId().getValue(),
                command.salesChannelId().getValue(),
                true
        );
    }

    @Override
    public Class<PublishProductToChannelCommand> getCommandType() {
        return PublishProductToChannelCommand.class;
    }

    private void requireVariantOnProduct(Product product, PublishProductToChannelCommand command) {
        boolean variantOnProduct = product.findVariantById(command.variantId()).isPresent();
        if (!variantOnProduct) {
            throw new CatalogServiceException(
                    new CatalogServiceError.VariantNotFound(command.variantId().getValue())
            );
        }
    }
}

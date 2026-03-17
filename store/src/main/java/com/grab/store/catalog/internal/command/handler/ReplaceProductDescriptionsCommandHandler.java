package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.command.GetProductPayload;
import com.grab.store.catalog.internal.command.ProductDescriptionsResult;
import com.grab.store.catalog.internal.command.ReplaceProductDescriptionsCommand;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReplaceProductDescriptionsCommandHandler implements CommandHandler<ReplaceProductDescriptionsCommand, ProductDescriptionsResult> {

    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public ProductDescriptionsResult handle(ReplaceProductDescriptionsCommand command) {
        Product product = loadProduct(command.productId());
        List<Description> descriptions = command.descriptions() == null
                ? List.of()
                : command.descriptions().stream()
                .map(description -> {
                    validateRequired(description.name(), "name");
                    validateRequired(description.description(), "description");
                    return new Description(
                            description.id(),
                            description.name(),
                            description.title(),
                            description.description()
                    );
                })
                .toList();

        product.replaceDescriptions(descriptions);
        productRepository.save(product);

        return new ProductDescriptionsResult(product.getId().getValue(), mapDescriptions(product.getDescriptions()));
    }

    @Override
    public Class<ReplaceProductDescriptionsCommand> getCommandType() {
        return ReplaceProductDescriptionsCommand.class;
    }

    private Product loadProduct(com.grab.framework.id.Id productId) {
        return productRepository.find(productId).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(productId.getValue()))
        );
    }

    private List<GetProductPayload.Description> mapDescriptions(List<Description> descriptions) {
        return descriptions.stream()
                .map(description -> new GetProductPayload.Description(
                        description.getId() == null ? null : new CommonId(description.getId().getValue()),
                        description.getName(),
                        description.getTitle(),
                        description.getDescription()
                ))
                .toList();
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.InvalidProductDescriptionPatch(fieldName + " is required")
            );
        }
    }
}

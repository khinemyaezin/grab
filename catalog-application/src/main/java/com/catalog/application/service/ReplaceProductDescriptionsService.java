package com.catalog.application.service;

import com.catalog.application.port.inbound.ReplaceProductDescriptionsUseCase;

import com.catalog.domain.aggregate.Description;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.catalog.application.model.write.GetProductPayload;
import com.catalog.application.model.write.ProductDescriptionsResult;
import com.catalog.application.model.write.ReplaceProductDescriptionsCommand;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

import java.util.List;

@lombok.RequiredArgsConstructor
public class ReplaceProductDescriptionsService implements ReplaceProductDescriptionsUseCase {

    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;

        public ProductDescriptionsResult execute(ReplaceProductDescriptionsCommand command) {
        Product product = loadProduct(command.productId(), command.merchantId());
        List<Description> descriptions = command.descriptions() == null
                ? List.of()
                : command.descriptions().stream()
                .map(description -> {
                    validateRequired(description.name(), "name");
                    validateRequired(description.description(), "description");
                    return new Description(
                            description.id() == null ? idGenerator.generateId() : description.id(),
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

    private Product loadProduct(com.grab.framework.id.Id productId, com.grab.framework.id.Id merchantId) {
        return productRepository.find(productId, merchantId).orElseThrow(() ->
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

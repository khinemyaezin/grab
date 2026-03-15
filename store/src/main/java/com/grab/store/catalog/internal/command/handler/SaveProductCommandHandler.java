package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import com.grab.store.catalog.internal.command.SaveProductResult;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.specification.UniqueSkuSpec;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaveProductCommandHandler implements CommandHandler<SaveProductCommand, SaveProductResult> {

    private static final Logger log = Loggers.getLogger(SaveProductCommandHandler.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UniqueSlugResolver uniqueSlugResolver;

    @Override
    @CatalogTransactional
    public SaveProductResult handle(SaveProductCommand command) {
        log.debug("Handling SaveProductCommand for product: {}", command.product().name());

        validateCategoryExists(command.product().categoryId());

        Product product = mapToDomainProduct(command.product());
        List<String> reservedSkus = new ArrayList<>();

        List<SaveProductCommand.Variant> variants = command.product().variants() == null
                ? List.of()
                : command.product().variants();

        for (SaveProductCommand.Variant variant : variants) {
            ProductVariant productVariant = mapToDomainVariant(variant);
            validateSkuAvailability(productVariant, reservedSkus, null);
            product.addVariant(productVariant);
            reservedSkus.add(productVariant.getSku());
        }

        productRepository.save(product);

        log.info("Product saved successfully with {} variantSummary", product.getVariants().size());

        return new SaveProductResult(product.getId().getValue());
    }

    @Override
    public Class<SaveProductCommand> getCommandType() {
        return SaveProductCommand.class;
    }

    private Product mapToDomainProduct(SaveProductCommand.Product product) {
        String slug = uniqueSlugResolver.resolve(product.slug(), product.name(), null);
        return Product.create(
                product.id(),
                product.name(),
                product.categoryId(),
                Boolean.TRUE.equals(product.featured()),
                slug
        );
    }

    private ProductVariant mapToDomainVariant(SaveProductCommand.Variant variant) {
        ProductVariantStatus status = mapToDomainProductVariantStatus(variant.status());
        List<ProductVariation> variations = mapToDomainProductVariations(variant.variations());
        return new ProductVariant(variant.id(), variant.sku(), status, variations);
    }



    private ProductVariantStatus mapToDomainProductVariantStatus(String status) {
        if (status == null || status.isBlank()) {
            return ProductVariantStatus.ACTIVE;
        }
        return ProductVariantStatus.valueOf(status);
    }

    private List<ProductVariation> mapToDomainProductVariations(List<SaveProductCommand.Variation> variations) {
        if (variations == null) {
            return List.of();
        }
        return variations.stream().map(v -> new ProductVariation(
                v.optionName(),
                v.optionId(),
                v.typeName(),
                v.typeId()
        )).toList();
    }

    private void validateCategoryExists(Id categoryId) {
        if (categoryRepository.find(categoryId).isEmpty()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.CategoryNotFound(categoryId.getValue())
            );
        }
    }

    private void validateSkuAvailability(ProductVariant variant, List<String> reservedSkus, String excludeVariantUuid) {
        if (!new UniqueSkuSpec(reservedSkus).isSatisfiedBy(variant)
                || productRepository.isSkuTaken(variant.getSku(), excludeVariantUuid)) {
            throw new CatalogServiceException(
                    new CatalogServiceError.SkuAlreadyExists(variant.getSku())
            );
        }
    }
}

package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import com.grab.store.catalog.internal.command.SaveProductResult;
import com.grab.framework.cqrs.command.CommandHandler;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.store.catalog.internal.util.UniqueSlugResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveProductCommandHandler implements CommandHandler<SaveProductCommand, SaveProductResult> {
    private final ProductRepository productRepository;
    private final UniqueSlugResolver uniqueSlugResolver;

    @Override
    @Transactional
    public SaveProductResult handle(SaveProductCommand command) {
        log.debug("Handling SaveProductCommand for product: {}", command.product().name());

        Product product = mapToDomainProduct(command.product());

        for (SaveProductCommand.Variant variant : command.product().variants()) {
            ProductVariant productVariant = mapToDomainVariant(variant, product.getId());
            product.addVariant(productVariant);
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

    private ProductVariant mapToDomainVariant(SaveProductCommand.Variant variant, Id productId) {
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
}

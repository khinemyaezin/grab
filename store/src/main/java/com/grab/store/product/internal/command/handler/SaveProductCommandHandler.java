package com.grab.store.product.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.command.SaveProductCommand;
import com.grab.store.product.internal.command.SaveProductResult;
import com.grab.store.product.internal.cqrs.command.CommandHandler;
import com.product.domain.aggregate.product.*;
import com.product.domain.repository.ProductRepository;
import com.product.domain.service.ProductVariantSynchronizer;
import com.product.domain.valueobject.ProductVariation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveProductCommandHandler implements CommandHandler<SaveProductCommand, SaveProductResult> {

    private final IdGenerator idGenerator;
    private final ProductRepository productRepository;
    private final ProductVariantSynchronizer productVariantSynchronizer;

    @Override
    @Transactional
    public SaveProductResult handle(SaveProductCommand command) {
        log.debug("Handling SaveProductCommand for product: {}", command.product().name());

        Product product = mapToDomainProduct(command.product());

        for (SaveProductCommand.Variant variant : command.product().variants()) {
            ProductVariant productVariant = mapToDomainVariant(variant, product.getId());
            product.addVariant(productVariant);
        }

        List<VariantType> desiredVariantTypes = command.variantTypes().stream()
                .map(this::mapToDomainProductVariantTypes).toList();

        productVariantSynchronizer.synchronize(product, desiredVariantTypes);

        productRepository.save(product);

        log.info("Product saved successfully with {} variants", product.getVariants().size());

        return new SaveProductResult(product.getId().getValue());
    }

    @Override
    public Class<SaveProductCommand> getCommandType() {
        return SaveProductCommand.class;
    }

    private Product mapToDomainProduct(SaveProductCommand.Product product) {
        Id productId = Objects.isNull(product.id()) ? idGenerator.generateId()
                : idGenerator.generateId(product.id());
        Id categoryId = idGenerator.generateId(product.categoryId());
        return Product.create(productId, product.name(), categoryId);
    }

    private ProductVariant mapToDomainVariant(SaveProductCommand.Variant variant, Id productId) {
        Id variantId = Objects.isNull(variant.id()) ? idGenerator.generateId()
                : idGenerator.generateId(variant.id());
        ProductVariantStatus status = mapToDomainProductVariantStatus(variant.status());
        List<ProductVariation> variations = mapToDomainProductVariations(variant.variations());
        return new ProductVariant(variantId, productId, variant.sku(), status, variations);
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
                idGenerator.generateId(v.optionId()),
                v.typeName(),
                idGenerator.generateId(v.typeId())
        )).toList();
    }

    private VariantType mapToDomainProductVariantTypes(SaveProductCommand.VariantType variantType) {
        VariantType domainVariantType = new VariantType(
                idGenerator.generateId(variantType.typeId()),
                variantType.typeName()
        );
        if (variantType.options() != null) {
            variantType.options().forEach(option ->
                    domainVariantType.addOption(mapToDomainProductVariantOptions(option, domainVariantType)));
        }
        return domainVariantType;
    }

    private VariantOption mapToDomainProductVariantOptions(SaveProductCommand.VariantOption option, VariantType variantType) {
        return new VariantOption(idGenerator.generateId(option.optionId()), option.optionName(), variantType);
    }
}
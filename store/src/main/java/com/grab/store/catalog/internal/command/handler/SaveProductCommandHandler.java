package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.*;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.valueobject.ListingCondition;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.SellerType;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.command.SaveProductCommand;
import com.grab.store.catalog.internal.command.SaveProductResult;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.specification.UniqueSkuSpec;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.util.CatalogPolicyValidator;
import com.grab.store.catalog.internal.util.StandaloneVariantDefaults;
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
    private final SkuGenerator skuGenerator;
    private final IdGenerator idGenerator;

    @Override
    @CatalogTransactional
    public SaveProductResult handle(SaveProductCommand command) {
        log.debug("Handling SaveProductCommand for product: {}", command.product().name());
        ensureProductIsNotExisted(command.product().name(), command.product().id());

        Product product = mapToDomainProduct(command.product());
        Category category = validateCategoryExists(command.product().categoryId());
        CatalogPolicyValidator.validateCategoryPolicy(category, product);
        List<ProductVariant> variants = materializeVariants(command);

        List<String> reservedSkus = new ArrayList<>();
        for (ProductVariant productVariant : variants) {
            validateSkuAvailability(productVariant, reservedSkus, null);
            if (!product.addVariant(productVariant)) {
                throw new CatalogServiceException(
                        new CatalogServiceError.VariantAddFailed(productVariant.getId().getValue())
                );
            }
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

    private List<ProductVariant> materializeVariants(SaveProductCommand command) {

        if (!isStandAloneVariant(command)) {
            return command.product().variants().stream()
                    .map(this::mapToDomainVariant)
                    .toList();
        }

        return List.of(createDefaultVariant(command));
    }

    private boolean isStandAloneVariant(SaveProductCommand command){
        return command.product().variants() == null || command.product().variants().isEmpty();
    }

    private void ensureProductIsNotExisted(String productName, Id productId){
        if(productRepository.find(productId).isPresent()) {
            throw new CatalogServiceException(new CatalogServiceError.ProductAlreadyExisted(productName));
        }
    }

    private Product mapToDomainProduct(SaveProductCommand.Product product) {
        Id productId = product.id() == null ? idGenerator.generateId() : product.id();
        String slug = uniqueSlugResolver.resolve(product.slug(), product.name(), null);
        return Product.create(
                productId,
                product.name(),
                product.categoryId(),
                product.sellerId(),
                mapSellerType(product.sellerType()),
                mapCondition(product.condition()),
                Boolean.TRUE.equals(product.offerEligible()),
                Boolean.TRUE.equals(product.featured()),
                slug,
                mapDescriptions(product.descriptions()),
                mapMedias(product.medias())
        );
    }

    private ProductVariant mapToDomainVariant(SaveProductCommand.Variant variant) {
        List<ProductVariation> variations = mapToDomainProductVariations(variant.variations());
        return ProductVariant.create(variant.id(), variant.sku(), variations);
    }

    private List<ProductVariation> mapToDomainProductVariations(List<SaveProductCommand.Variation> variations) {
        if (variations == null || variations.isEmpty()) {
            return List.of();
        }
        return variations.stream().map(v -> new ProductVariation(
                v.optionName(),
                v.optionId(),
                v.typeName(),
                v.typeId()
        )).toList();
    }

    private ProductVariant createDefaultVariant(SaveProductCommand command) {
        List<ProductVariation> variations = StandaloneVariantDefaults.defaultVariations();
        return ProductVariant.create(
                idGenerator.generateId(),
                generateSku(command.product().name(), variations),
                variations
        );
    }

    private String generateSku(String productName, List<ProductVariation> variations) {
        return skuGenerator.generate(new SkuGenerator.Context(productName, variations));
    }

    private List<Description> mapDescriptions(List<SaveProductCommand.Description> descriptions) {
        if (descriptions == null) {
            return List.of();
        }
        return descriptions.stream()
                .map(d -> new Description(null, d.name(), d.title(), d.description()))
                .toList();
    }

    private List<ProductMedia> mapMedias(List<SaveProductCommand.Media> medias) {
        if (medias == null) {
            return List.of();
        }
        return medias.stream()
                .map(media -> new ProductMedia(null, media.type(), media.path()))
                .toList();
    }

    private SellerType mapSellerType(String sellerType) {
        return sellerType == null ? null : SellerType.valueOf(sellerType);
    }

    private ListingCondition mapCondition(String condition) {
        return condition == null || condition.isBlank() ? null : ListingCondition.valueOf(condition);
    }

    private Category validateCategoryExists(Id categoryId) {
        return categoryRepository.find(categoryId).orElseThrow(() -> new CatalogServiceException(
                new CatalogServiceError.CategoryNotFound(categoryId.getValue())
        ));
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

package com.grab.store.catalog.internal.query.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.ProductCombinationQuery;
import com.grab.store.catalog.internal.query.ProductCombinationResult;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.aggregate.VariantOption;
import com.catalog.domain.aggregate.VariantType;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.VariantDeletionStrategy;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductCombinationQueryHandler implements QueryHandler<ProductCombinationQuery, ProductCombinationResult> {

    private static final Logger log = Loggers.getLogger(ProductCombinationQueryHandler.class);
    private final IdGenerator idGenerator;
    private final VariationCombinationManager variationCombinationManager;
    private final VariantCombinationService variantCombinationService;
    private final VariantDeletionStrategy variantDeletionStrategy;
    private final SkuGenerator skuGenerator;

    @Override
    @CatalogReadTransactional
    public ProductCombinationResult handle(ProductCombinationQuery query) {
        log.debug("Handling BuildProductQuery for product: {}", query.product().name());

        List<VariantType> variantTypes = query.variantTypes().stream()
                .map(this::mapToDomainProductVariantTypes)
                .toList();
        Id productId = generateProductId(query.product().id());
        List<ProductVariant> domainVariants;

        if (query.product().variants().isEmpty()) {
            domainVariants = generateNewVariants(productId, query.product().name(), variantTypes);
        } else {
            domainVariants = synchronizeVariants(query, productId, variantTypes);
        }

        return mapToResult(productId, query.product().name(), query.product().categoryId(),
                domainVariants, variantTypes);
    }

    private Id generateProductId(Id existingId) {
        return Objects.nonNull(existingId) ? existingId : idGenerator.generateId();
    }

    private List<ProductVariant> generateNewVariants(Id productId, String productName, List<VariantType> variantTypes) {
        List<List<VariantOption>> combinations = variantCombinationService.generateCombinations(variantTypes);
        return combinations.stream()
                .map(options -> createNewVariant(productId,productName, options))
                .toList();
    }

    private List<ProductVariant> synchronizeVariants(ProductCombinationQuery query, Id productId, List<VariantType> variantTypes) {
        List<ProductVariant> existingVariants = query.product().variants().stream()
                .map(variant -> mapToDomainVariant(variant, productId))
                .toList();

        List<VariantType> filteredVariantTypes = variantDeletionStrategy.filterVariantTypes(existingVariants, variantTypes);
        List<List<VariantOption>> combinations = variantCombinationService.generateCombinations(filteredVariantTypes);
        List<VariantCombination> newVariantCombinations = convertToVariantCombination(combinations);

        List<VariationCombinationManager.VariantCombinationResult> results =
                variationCombinationManager.syncCombinations(existingVariants, newVariantCombinations);

        return results.stream()
                .map(result -> mapVariantCombinationResult(result, query.product().name()))
                .toList();
    }

    private ProductVariant mapVariantCombinationResult(
            VariationCombinationManager.VariantCombinationResult result,
            String productName) {
        if (result.matchedType() == VariationCombinationManager.VariantCombinationResult.MatchedType.UNCHANGED) {
            return result.matchedVariant();
        }
        return new ProductVariant(
                idGenerator.generateId(),
                generateSku(productName, result.variantCombination().getVariations()),
                ProductVariantStatus.ACTIVE,
                result.variantCombination().getVariations()
        );
    }

    private ProductVariant createNewVariant(Id productId, String productName, List<VariantOption> options) {
        List<ProductVariation> variations = mapOptionsToVariations(options);
        return new ProductVariant(
                idGenerator.generateId(),
                generateSku(productName, variations),
                ProductVariantStatus.ACTIVE,
                variations
        );
    }

    private List<ProductVariation> mapOptionsToVariations(List<VariantOption> options) {
        return options.stream()
                .map(opt -> new ProductVariation(
                        opt.getName(),
                        opt.getId(),
                        opt.getVariantType().getName(),
                        opt.getVariantType().getId()
                ))
                .toList();
    }

    @Override
    public Class<ProductCombinationQuery> getQueryType() {
        return ProductCombinationQuery.class;
    }


    private ProductVariant mapToDomainVariant(ProductCombinationQuery.Variant variant, Id productId) {
        Id variantId = Objects.isNull(variant.id()) ? idGenerator.generateId()
                : idGenerator.generateId(variant.id());
        ProductVariantStatus status = mapToDomainProductVariantStatus(variant.status());
        List<ProductVariation> variations = mapToDomainProductVariations(variant.variations());
        return new ProductVariant(variantId, variant.sku(), status, variations);
    }

    private ProductVariantStatus mapToDomainProductVariantStatus(String status) {
        if (status == null || status.isBlank()) {
            return ProductVariantStatus.ACTIVE;
        }
        return ProductVariantStatus.valueOf(status);
    }

    private List<ProductVariation> mapToDomainProductVariations(List<ProductCombinationQuery.Variation> variations) {
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

    private VariantType mapToDomainProductVariantTypes(ProductCombinationQuery.VariantType variantType) {
        VariantType domainVariantType = new VariantType(idGenerator.generateId(variantType.typeId()), variantType.typeName());
        variantType.options()
                .forEach(option ->
                        domainVariantType.addOption(mapToDomainProductVariantOptions(option, domainVariantType)));
        return domainVariantType;
    }

    private VariantOption mapToDomainProductVariantOptions(ProductCombinationQuery.VariantOption option, VariantType variantType) {
        return new VariantOption(idGenerator.generateId(option.optionId()), option.optionName(), variantType);
    }

    private String generateSku(String productName,  List<ProductVariation> variations) {
        SkuGenerator.Context ctx = new SkuGenerator.Context(
                productName,
                variations
        );
        return skuGenerator.generate(ctx);
    }

    private ProductCombinationResult.Variant mapToResultVariant(ProductVariant variant) {
        List<ProductCombinationResult.Variation> variations = variant.getVariations().stream()
                .map(this::mapToResultVariation)
                .toList();

        return new ProductCombinationResult.Variant(
                variant.getId().getValue(),
                variant.getSku(),
                variant.getStatus().name(),
                variations
        );
    }

    private ProductCombinationResult.Variation mapToResultVariation(ProductVariation variation) {
        return new ProductCombinationResult.Variation(
                variation.getOptionName(),
                variation.getOptionId().getValue(),
                variation.getTypeId().getValue(),
                variation.getTypeName()
        );
    }

    private List<ProductCombinationResult.VariantType> mapToResultVariantTypes(List<VariantType> variantTypes) {
        return variantTypes.stream()
                .map(this::mapToResultVariantType)
                .toList();
    }

    private ProductCombinationResult.VariantType mapToResultVariantType(VariantType variantType) {
        List<ProductCombinationResult.VariantOption> options = variantType.getOptions().stream()
                .map(this::mapToResultVariantOption)
                .toList();

        return new ProductCombinationResult.VariantType(
                variantType.getId().getValue(),
                variantType.getName(),
                options
        );
    }

    private ProductCombinationResult.VariantOption mapToResultVariantOption(VariantOption option) {
        return new ProductCombinationResult.VariantOption(
                option.getId().getValue(),
                option.getName()
        );
    }

    private List<VariantCombination> convertToVariantCombination(List<List<VariantOption>> combinations) {
        return combinations.stream()
                .map(options -> new VariantCombination(mapOptionsToVariations(options)))
                .toList();
    }

    private ProductCombinationResult mapToResult(Id productId, String productName, Id categoryId,
                                                  List<ProductVariant> variants, List<VariantType> variantTypes) {
        List<ProductCombinationResult.Variant> resultVariants = variants.stream()
                .map(this::mapToResultVariant)
                .toList();

        return new ProductCombinationResult(
                new ProductCombinationResult.Product(
                        productId,
                        productName,
                        categoryId,
                        resultVariants
                ),
                mapToResultVariantTypes(variantTypes)
        );
    }

}

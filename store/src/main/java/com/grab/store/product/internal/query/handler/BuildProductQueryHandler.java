package com.grab.store.product.internal.query.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.specification.CompositeSpecification;
import com.grab.store.product.internal.cqrs.query.QueryHandler;
import com.grab.store.product.internal.query.BuildProductQuery;
import com.grab.store.product.internal.query.BuildProductResult;
import com.grab.store.product.internal.query.specification.BuildProductSpec;
import com.product.domain.aggregate.product.*;
import com.product.domain.service.ProductVariantSynchronizer;
import com.product.domain.valueobject.ProductVariation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildProductQueryHandler implements QueryHandler<BuildProductQuery, BuildProductResult> {
    private final IdGenerator idGenerator;
    private final ProductVariantSynchronizer productVariantSynchronizer;

    @Override
    public BuildProductResult handle(BuildProductQuery query) {
        log.debug("Handling BuildProductQuery for product: {}", query.product().name());

        CompositeSpecification<BuildProductQuery> spec = new BuildProductSpec();
        if (!spec.isSatisfiedBy(query)) {
            throw new RuntimeException("BuildProductQuery validation failed");
        }

        Product product = mapToDomainProduct(query.product());
        for (BuildProductQuery.Variant variant : query.product().variants()) {
            ProductVariant productVariant = mapToDomainVariant(variant, product.getId());
            product.addVariant(productVariant);
        }

        List<VariantType> desiredVariantTypes = query.variantTypes().stream()
                .map(this::mapToDomainProductVariantTypes).toList();

        productVariantSynchronizer.synchronize(product, desiredVariantTypes);

        log.info("Build product completed with {} variants", product.getVariants().size());

        return mapToResult(product, desiredVariantTypes);
    }

    @Override
    public Class<BuildProductQuery> getQueryType() {
        return BuildProductQuery.class;
    }

    private Product mapToDomainProduct(BuildProductQuery.Product product) {
        Id productId = Objects.isNull(product.id()) ? idGenerator.generateId()
                : idGenerator.generateId(product.id());
        Id categoryId = idGenerator.generateId(product.categoryId());
        return new Product(productId, product.name(), categoryId);
    }

    private ProductVariant mapToDomainVariant(BuildProductQuery.Variant variant, Id productId) {
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

    private List<ProductVariation> mapToDomainProductVariations(List<BuildProductQuery.Variation> variations) {
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

    private VariantType mapToDomainProductVariantTypes(BuildProductQuery.VariantType variantType) {
        VariantType domainVariantType = new VariantType(idGenerator.generateId(variantType.typeId()), variantType.typeName());
        variantType.options()
                .forEach(option ->
                        domainVariantType.addOption(mapToDomainProductVariantOptions(option, domainVariantType)));
        return domainVariantType;
    }

    private VariantOption mapToDomainProductVariantOptions(BuildProductQuery.VariantOption option, VariantType variantType) {
        return new VariantOption(idGenerator.generateId(option.optionId()), option.optionName(), variantType);
    }

    // Mapping from Domain to Result

    private BuildProductResult mapToResult(Product product, List<VariantType> variantTypes) {
        return new BuildProductResult(
                mapToResultProduct(product),
                mapToResultVariantTypes(variantTypes)
        );
    }

    private BuildProductResult.Product mapToResultProduct(Product product) {
        List<BuildProductResult.Variant> variants = product.getVariants().stream()
                .map(this::mapToResultVariant)
                .toList();

        return new BuildProductResult.Product(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                variants
        );
    }

    private BuildProductResult.Variant mapToResultVariant(ProductVariant variant) {
        List<BuildProductResult.Variation> variations = variant.getVariations().stream()
                .map(this::mapToResultVariation)
                .toList();

        return new BuildProductResult.Variant(
                variant.getId().getValue(),
                variant.getSku(),
                variant.getStatus().name(),
                variations
        );
    }

    private BuildProductResult.Variation mapToResultVariation(ProductVariation variation) {
        return new BuildProductResult.Variation(
                variation.getOptionName(),
                variation.getOptionId().getValue(),
                variation.getTypeId().getValue(),
                variation.getTypeName()
        );
    }

    private List<BuildProductResult.VariantType> mapToResultVariantTypes(List<VariantType> variantTypes) {
        return variantTypes.stream()
                .map(this::mapToResultVariantType)
                .toList();
    }

    private BuildProductResult.VariantType mapToResultVariantType(VariantType variantType) {
        List<BuildProductResult.VariantOption> options = variantType.getOptions().stream()
                .map(this::mapToResultVariantOption)
                .toList();

        return new BuildProductResult.VariantType(
                variantType.getId().getValue(),
                variantType.getName(),
                options
        );
    }

    private BuildProductResult.VariantOption mapToResultVariantOption(VariantOption option) {
        return new BuildProductResult.VariantOption(
                option.getId().getValue(),
                option.getName()
        );
    }
}

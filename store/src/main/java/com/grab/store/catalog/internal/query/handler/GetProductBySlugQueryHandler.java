package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.aggregate.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.GetProductBySlugQuery;
import com.grab.store.catalog.internal.query.GetProductBySlugResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductBySlugQueryHandler implements QueryHandler<GetProductBySlugQuery, GetProductBySlugResult> {

    private final ProductRepository productRepository;

    @Override
    @CatalogReadTransactional
    public GetProductBySlugResult handle(GetProductBySlugQuery query) {
        log.debug("Handling GetProductBySlugQuery for slug: {}", query.slug());

        Product product = productRepository.findBySlug(query.slug())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFoundBySlug(query.slug())
                ));

        if (!product.isVisibleOnStorefront()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductNotFoundBySlug(query.slug())
            );
        }

        return mapToSlugResult(product);
    }

    @Override
    public Class<GetProductBySlugQuery> getQueryType() {
        return GetProductBySlugQuery.class;
    }

    public GetProductBySlugResult mapToSlugResult(Product product) {
        List<GetProductBySlugResult.Variant> variants = product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .map(this::mapToSlugResultVariant)
                .toList();

        List<GetProductBySlugResult.VariantType> variantTypes = extractSlugVariantTypes(product);

        return new GetProductBySlugResult(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                product.getStatus().name(),
                product.getSlug(),
                product.isFeatured(),
                variants,
                variantTypes
        );
    }

    private List<GetProductBySlugResult.VariantType> extractSlugVariantTypes(Product product) {
        Map<String, List<GetProductBySlugResult.VariantOption>> typeOptionsMap = new LinkedHashMap<>();
        Map<String, String> typeNameMap = new LinkedHashMap<>();

        for (ProductVariant variant : product.getVariants()) {
            if (variant.getStatus() != ProductVariantStatus.ACTIVE) {
                continue;
            }
            for (ProductVariation variation : variant.getVariations()) {
                String typeId = variation.getTypeId().getValue();
                typeNameMap.putIfAbsent(typeId, variation.getTypeName());

                typeOptionsMap.computeIfAbsent(typeId, k -> new ArrayList<>());
                List<GetProductBySlugResult.VariantOption> options = typeOptionsMap.get(typeId);

                boolean exists = options.stream()
                        .anyMatch(o -> o.optionId().equals(variation.getOptionId().getValue()));
                if (!exists) {
                    options.add(new GetProductBySlugResult.VariantOption(
                            variation.getOptionId().getValue(),
                            variation.getOptionName()
                    ));
                }
            }
        }

        return typeOptionsMap.entrySet().stream()
                .map(entry -> new GetProductBySlugResult.VariantType(
                        entry.getKey(),
                        typeNameMap.get(entry.getKey()),
                        entry.getValue()
                ))
                .toList();
    }

    private GetProductBySlugResult.Variant mapToSlugResultVariant(ProductVariant variant) {
        List<GetProductBySlugResult.Variation> variations = variant.getVariations().stream()
                .map(this::mapToSlugResultVariation)
                .toList();

        return new GetProductBySlugResult.Variant(
                variant.getId().getValue(),
                variant.getSku(),
                variant.getStatus().name(),
                variations
        );
    }

    private GetProductBySlugResult.Variation mapToSlugResultVariation(ProductVariation variation) {
        return new GetProductBySlugResult.Variation(
                variation.getOptionId().getValue(),
                variation.getOptionName(),
                variation.getTypeId().getValue(),
                variation.getTypeName()
        );
    }
}

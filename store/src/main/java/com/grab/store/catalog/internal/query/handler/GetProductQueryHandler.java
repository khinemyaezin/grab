package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.ProductMedia;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.GetProductQuery;
import com.grab.store.catalog.internal.query.GetProductResult;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, GetProductResult> {

    private static final Logger log = Loggers.getLogger(GetProductQueryHandler.class);

    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;

    @Override
    @CatalogReadTransactional
    public GetProductResult handle(GetProductQuery query) {
        log.debug("Handling GetProductQuery for productId: {}", query.productId());

        Product product = productRepository.find(idGenerator.convertIdFrom(query.productId()))
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));

        return mapToResult(product);
    }

    @Override
    public Class<GetProductQuery> getQueryType() {
        return GetProductQuery.class;
    }

    public  GetProductResult mapToResult(Product product) {
        List<GetProductResult.Variant> variants = product.getVariants().stream()
                .map(this::mapToResultVariant)
                .toList();

        List<GetProductResult.VariantType> variantTypes = extractVariantTypes(product);

        return new GetProductResult(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                product.getSellerId() == null ? null : product.getSellerId().getValue(),
                product.getSellerType() == null ? null : product.getSellerType().name(),
                product.getListingCondition() == null ? null : product.getListingCondition().name(),
                product.isOfferEligible(),
                product.getStatus().name(),
                product.getSlug(),
                product.isFeatured(),
                product.getDescriptions().stream()
                        .map(description -> new GetProductResult.Description(
                                description.getId() == null ? null : description.getId().getValue(),
                                description.getName(),
                                description.getTitle(),
                                description.getDescription()
                        ))
                        .toList(),
                product.getMedias().stream()
                        .map(this::mapToResultMedia)
                        .toList(),
                product.getModerationNote(),
                variants,
                variantTypes
        );
    }

    private List<GetProductResult.VariantType> extractVariantTypes(Product product) {
        Map<String, List<GetProductResult.VariantOption>> typeOptionsMap = new LinkedHashMap<>();
        Map<String, String> typeNameMap = new LinkedHashMap<>();

        for (ProductVariant variant : product.getVariants()) {
            for (ProductVariation variation : variant.getVariations()) {
                String typeId = variation.getTypeId().getValue();
                typeNameMap.putIfAbsent(typeId, variation.getTypeName());

                typeOptionsMap.computeIfAbsent(typeId, k -> new ArrayList<>());
                List<GetProductResult.VariantOption> options = typeOptionsMap.get(typeId);

                boolean exists = options.stream()
                        .anyMatch(o -> o.optionId().equals(variation.getOptionId().getValue()));
                if (!exists) {
                    options.add(new GetProductResult.VariantOption(
                            variation.getOptionId().getValue(),
                            variation.getOptionName()
                    ));
                }
            }
        }

        return typeOptionsMap.entrySet().stream()
                .map(entry -> new GetProductResult.VariantType(
                        entry.getKey(),
                        typeNameMap.get(entry.getKey()),
                        entry.getValue()
                ))
                .toList();
    }

    private GetProductResult.Variant mapToResultVariant(ProductVariant variant) {
        List<GetProductResult.Variation> variations = variant.getVariations().stream()
                .map(this::mapToResultVariation)
                .toList();

        return new GetProductResult.Variant(
                variant.getId().getValue(),
                variant.getSku(),
                variant.getStatus().name(),
                variations
        );
    }

    private GetProductResult.Variation mapToResultVariation(ProductVariation variation) {
        return new GetProductResult.Variation(
                variation.getOptionId().getValue(),
                variation.getOptionName(),
                variation.getTypeId().getValue(),
                variation.getTypeName()
        );
    }

    private GetProductResult.Media mapToResultMedia(ProductMedia media) {
        return new GetProductResult.Media(
                media.getId() == null ? null : media.getId().getValue(),
                media.getType(),
                media.getPath()
        );
    }
}

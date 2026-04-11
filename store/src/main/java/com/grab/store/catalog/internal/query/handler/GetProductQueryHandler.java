package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.VariantOptionView;
import com.grab.framework.id.Id;
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
import com.grab.store.catalog.internal.util.ParentChildTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, GetProductResult> {

    private static final Logger log = Loggers.getLogger(GetProductQueryHandler.class);

    private final ProductRepository productRepository;
    private final VariantOptionQueryRepository variantOptionQueryRepository;
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
        List<ProductVariation> allVariations = product.getVariants().stream()
                .flatMap(v -> v.getVariations().stream())
                .toList();

        List<String> optionIds = allVariations.stream()
                .map(v -> v.getOptionId().getValue())
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = fetchVariantOptions(optionIds);

        List<GetProductResult.Variant> variants = mapToResultVariantList(product.getVariants(), optionViews);

        List<GetProductResult.VariantType> variantTypes = extractVariantTypes(optionViews);

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

    private List<GetProductResult.VariantType> extractVariantTypes(List<VariantOptionView> optionViews) {
        ParentChildTransformer<VariantOptionView, Id, GetProductResult.VariantType, GetProductResult.VariantOption> transformer =
                ParentChildTransformer.of(
                        view -> idGenerator.convertIdFrom(view.typeId()),
                        view -> new GetProductResult.VariantType(view.typeId(), view.typeName(), new ArrayList<>()),
                        view -> new GetProductResult.VariantOption(view.optionId(), view.optionName()),
                        (parent, child) -> {
                            List<GetProductResult.VariantOption> options = new ArrayList<>(parent.options());
                            options.add(child);
                            return new GetProductResult.VariantType(parent.typeId(), parent.typeName(), options);
                        }
                );
        return transformer.apply(optionViews);

    }

    private List<VariantOptionView> fetchVariantOptions(List<String> optionIds) {
        return variantOptionQueryRepository.findAllByUuidIn(optionIds);
    }

    private List<GetProductResult.Variant> mapToResultVariantList(List<ProductVariant> variantList, List<VariantOptionView> optionViews) {
        Map<String, VariantOptionView> variationMapByOptionId = optionViews.stream()
                .collect(Collectors.toMap(
                        VariantOptionView::optionId,
                        Function.identity()));
        List<GetProductResult.Variant> result = new ArrayList<>(variantList.size());
        for(ProductVariant variant : variantList) {
            List<GetProductResult.Variation> variations = new ArrayList<>();
            for(ProductVariation productVariation : variant.getVariations()) {
                VariantOptionView variantOptionView = variationMapByOptionId.get(productVariation.getOptionId().getValue());
                GetProductResult.Variation variation = new GetProductResult.Variation(
                        variantOptionView.optionId(),
                        variantOptionView.optionName(),
                        variantOptionView.typeId(),
                        variantOptionView.typeName()
                );
                variations.add(variation);
            }

            GetProductResult.Variant resultVariant = new GetProductResult.Variant(
                    variant.getId().getValue(),
                    variant.getSku(),
                    variant.getStatus().name(),
                    variations
            );
            result.add(resultVariant);
        }
        return result;
    }

    private GetProductResult.Media mapToResultMedia(ProductMedia media) {
        return new GetProductResult.Media(
                media.getId() == null ? null : media.getId().getValue(),
                media.getType(),
                media.getPath()
        );
    }
}

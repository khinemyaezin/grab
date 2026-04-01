package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.VariantOptionView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.GetProductBySlugQuery;
import com.grab.store.catalog.internal.query.GetProductBySlugResult;
import com.grab.store.catalog.internal.util.ParentChildTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetProductBySlugQueryHandler implements QueryHandler<GetProductBySlugQuery, GetProductBySlugResult> {

    private static final Logger log = Loggers.getLogger(GetProductBySlugQueryHandler.class);

    private final ProductRepository productRepository;
    private final VariantOptionQueryRepository variantOptionQueryRepository;
    private final IdGenerator idGenerator;

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
        List<ProductVariation> allVariations = product.getVariants().stream()
                .flatMap(v -> v.getVariations().stream())
                .toList();

        List<String> optionIds = allVariations.stream()
                .map(v -> v.getOptionId().getValue())
                .distinct()
                .toList();

        List<VariantOptionView> optionViews = fetchVariantOptions(optionIds);

        List<GetProductBySlugResult.Variant> variants = mapToSlugResultVariant(product.getVariants(), optionViews);

        List<GetProductBySlugResult.VariantType> variantTypes = extractSlugVariantTypes(optionViews);

        return new GetProductBySlugResult(
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
                        .map(description -> new GetProductBySlugResult.Description(
                                description.getId() == null ? null : description.getId().getValue(),
                                description.getName(),
                                description.getTitle(),
                                description.getDescription()
                        ))
                        .toList(),
                product.getMedias().stream()
                        .map(this::mapToSlugResultMedia)
                        .toList(),
                product.getModerationNote(),
                variants,
                variantTypes
        );
    }

    private List<VariantOptionView> fetchVariantOptions(List<String> optionIds) {
        return variantOptionQueryRepository.findAllByUuidIn(optionIds);
    }

    private List<GetProductBySlugResult.VariantType> extractSlugVariantTypes(List<VariantOptionView> optionViews) {
        ParentChildTransformer<VariantOptionView, Id, GetProductBySlugResult.VariantType, GetProductBySlugResult.VariantOption> transformer =
                ParentChildTransformer.of(
                        view -> idGenerator.convertIdFrom(view.typeId()),
                        view -> new GetProductBySlugResult.VariantType(view.typeId(), view.typeName(), new ArrayList<>()),
                        view -> new GetProductBySlugResult.VariantOption(view.optionId(), view.optionName()),
                        (parent, child) -> {
                            List<GetProductBySlugResult.VariantOption> options = new ArrayList<>(parent.options());
                            options.add(child);
                            return new GetProductBySlugResult.VariantType(parent.typeId(), parent.typeName(), options);
                        }
                );
        return transformer.apply(optionViews);
    }

    private List<GetProductBySlugResult.Variant> mapToSlugResultVariant(List<ProductVariant> variantList, List<VariantOptionView> optionViews) {
        Map<String, VariantOptionView> variationMapByOptionId = optionViews.stream()
                .collect(Collectors.toMap(
                        VariantOptionView::optionId,
                        Function.identity()));
        List<GetProductBySlugResult.Variant> result = new ArrayList<>(variantList.size());

        for(ProductVariant variant : variantList) {
            List<GetProductBySlugResult.Variation> variations = new ArrayList<>();
            for(ProductVariation productVariation : variant.getVariations()) {
                VariantOptionView variantOptionView = variationMapByOptionId.get(productVariation.getOptionId().getValue());
                GetProductBySlugResult.Variation variation = new GetProductBySlugResult.Variation(
                        variantOptionView.optionId(),
                        variantOptionView.optionName(),
                        variantOptionView.typeId(),
                        variantOptionView.typeName()
                );
                variations.add(variation);
            }

            GetProductBySlugResult.Variant resultVariant = new GetProductBySlugResult.Variant(
                    variant.getId().getValue(),
                    variant.getSku(),
                    variant.getStatus().name(),
                    variations
            );
            result.add(resultVariant);
        }
        return result;
    }


    private GetProductBySlugResult.Media mapToSlugResultMedia(ProductMedia media) {
        return new GetProductBySlugResult.Media(
                media.getId() == null ? null : media.getId().getValue(),
                media.getType(),
                media.getPath()
        );
    }
}

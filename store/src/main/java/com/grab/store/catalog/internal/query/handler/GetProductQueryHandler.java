package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.CategoryView;
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
import com.grab.store.catalog.internal.util.StandaloneVariationFactory;
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
    private final CategoryQueryRepository categoryQueryRepository;
    private final MatrixKeyGenerator matrixKeyGenerator;

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

    public GetProductResult mapToResult(Product product) {
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
        GetProductResult.Category category = findCategoryById(product.getCategoryId())
                .map(this::mapToCategory)
                .orElse(null);

        return new GetProductResult(
                product.getId().getValue(),
                product.getName(),
                category,
                product.getListingCondition() == null ? null : product.getListingCondition().name(),
                product.getStatus().name(),
                product.getSlug(),
                variants,
                variantTypes
        );
    }

    private List<GetProductResult.VariantType> extractVariantTypes(List<VariantOptionView> optionViews) {
        if (optionViews == null || optionViews.isEmpty()) return Collections.emptyList();

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
        Map<String, VariantOptionView> variationMapByOptionId = Optional.ofNullable(optionViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(VariantOptionView::optionId, Function.identity(), (a, b) -> a));

        return variantList.stream().map(variant -> {
            List<GetProductResult.Variation> variations = variant.getVariations().stream()
                    .map(pv -> mapToVariation(pv, variationMapByOptionId))
                    .filter(Objects::nonNull)
                    .toList();

            String matrixKey = matrixKeyGenerator.generateKey(new ArrayList<>(variant.getVariations()));

            return new GetProductResult.Variant(
                    variant.getId().getValue(),
                    variant.getSku(),
                    variant.getStatus().name(),
                    matrixKey,
                    variations
            );
        }).toList();
    }

    private GetProductResult.Variation mapToVariation(ProductVariation pv, Map<String, VariantOptionView> viewMap) {
        if (StandaloneVariationFactory.isStandAloneVariation(pv)) {
            return null;
        }

        String optId = Optional.ofNullable(pv.getOptionId()).map(Id::getValue).orElse(null);
        VariantOptionView view = viewMap.get(optId);

        return (view != null)
                ? new GetProductResult.Variation(view.optionId(), view.optionName(), view.typeId(), view.typeName())
                : new GetProductResult.Variation(optId, "", pv.getTypeId().getValue(), "");
    }

    private Optional<CategoryView> findCategoryById(Id id) {
        List<CategoryView> matchedCategoryViews = this.categoryQueryRepository.findViewByIds(List.of(id.getValue()));
        if (!matchedCategoryViews.isEmpty()) {
            return Optional.of(matchedCategoryViews.getFirst());
        }
        log.error("Category id {} not found", id);
        return Optional.empty();
    }

    private GetProductResult.Category mapToCategory(CategoryView categoryView) {
        return new GetProductResult.Category(categoryView.id(), categoryView.name());
    }
}

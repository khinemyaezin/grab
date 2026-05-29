package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.ProductSummary;
import com.catalog.infrastructure.view.VariantOptionView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.ProductSummaryQuery;
import com.grab.store.catalog.internal.query.ProductSummaryResult;
import com.grab.store.catalog.internal.query.SpringPageInfoFactory;
import com.grab.store.catalog.internal.util.StandaloneVariationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductSummaryQueryHandler implements QueryHandler<ProductSummaryQuery, ProductSummaryResult> {

    private static final Logger log = Loggers.getLogger(ProductSummaryQueryHandler.class);

    private final ProductQueryRepository productQueryRepository;
    private final VariantOptionQueryRepository variantOptionQueryRepository;

    @Override
    @CatalogReadTransactional
    public ProductSummaryResult handle(ProductSummaryQuery query) {
        log.debug("Handling ProductSummaryQuery");

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .productName(query.productName())
                .sku(query.sku())
                .variantStatus(query.variantStatus())
                .categoryId(query.categoryId())
                .build();

        Page<ProductSummary> page = productQueryRepository.search(criteria, PageRequest.of(query.page(), query.size()));
        List<ProductSummaryResult.Product> products = mapToResultProducts(page.getContent());

        return new ProductSummaryResult(
                products,
                SpringPageInfoFactory.toPageInfo(page));
    }

    @Override
    public Class<ProductSummaryQuery> getQueryType() {
        return ProductSummaryQuery.class;
    }

    private List<ProductSummaryResult.Product> mapToResultProducts(List<ProductSummary> summaries) {
        List<String> optionIds = summaries.stream()
                .flatMap(v -> v.variantSummary().types().stream())
                .flatMap(type -> type.options().stream())
                .map(ProductSummary.VariantOption::optionId)
                .toList();

        List<VariantOptionView> optionViews = fetchVariantOptions(optionIds);

        Map<String, VariantOptionView> variationMapByOptionId = Optional.ofNullable(optionViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(VariantOptionView::optionId, Function.identity(), (a, b) -> a));


        return summaries.stream()
                .map(summary -> new ProductSummaryResult.Product(
                        summary.id(),
                        summary.name(),
                        summary.status(),
                        summary.slug(),
                        new ProductSummaryResult.VariantSummary(
                                summary.variantSummary().available(),
                                extractVariantTypes(summary.variantSummary(), variationMapByOptionId)
                        )
                ))
                .toList();
    }

    private List<ProductSummaryResult.VariantType> extractVariantTypes(ProductSummary.VariantSummary variantSummary, Map<String, VariantOptionView> variationMapByOptionId) {
        return variantSummary.types().stream()
                .filter( type-> !StandaloneVariationFactory.isStandAloneVariation(type.typeId()))
                .map(type -> {
                    String typeName = type.options().stream()
                            .findFirst()
                            .map(option -> variationMapByOptionId.get(option.optionId()))
                            .map(VariantOptionView::typeName)
                            .orElse("");

                    return new ProductSummaryResult.VariantType(
                            type.typeId(),
                            typeName,
                            type.options().stream().map(option -> {
                                VariantOptionView view = variationMapByOptionId.get(option.optionId());
                                String optionName = (view != null) ? view.optionName() : "";

                                return new ProductSummaryResult.VariantOption(
                                        option.optionId(),
                                        optionName
                                );
                            }).toList()
                    );
                })
                .toList();
    }

    private List<VariantOptionView> fetchVariantOptions(List<String> optionIds) {
        return variantOptionQueryRepository.findAllByUuidIn(optionIds);
    }
}

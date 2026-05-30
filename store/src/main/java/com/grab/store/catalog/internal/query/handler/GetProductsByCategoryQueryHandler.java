package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.valueobject.ProductStatus;
import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.CategoryView;
import com.catalog.infrastructure.view.ProductSummary;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.GetProductsByCategoryQuery;
import com.grab.store.catalog.internal.query.ProductSummaryResult;
import com.grab.store.catalog.internal.query.SpringPageInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetProductsByCategoryQueryHandler implements QueryHandler<GetProductsByCategoryQuery, ProductSummaryResult> {

    private static final Logger log = Loggers.getLogger(GetProductsByCategoryQueryHandler.class);

    private final ProductQueryRepository productQueryRepository;
    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    @CatalogReadTransactional
    public ProductSummaryResult handle(GetProductsByCategoryQuery query) {
        log.debug("Handling GetProductsByCategoryQuery for categoryId={}", query.categoryId());

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .categoryId(query.categoryId())
                .productStatus(ProductStatus.ACTIVE.name())
                .build();

        Page<ProductSummary> page = productQueryRepository.search(
                criteria,
                PageRequest.of(query.page(), query.size())
        );

        List<ProductSummaryResult.Product> products = mapToResultProducts(page.getContent());

        return new ProductSummaryResult(products, SpringPageInfoFactory.toPageInfo(page));
    }

    private List<ProductSummaryResult.Product> mapToResultProducts(List<ProductSummary> summaries) {
        List<String> categoryIds = summaries.stream().map(ProductSummary::categoryId).toList();
        List<CategoryView> categoryViews = fetchCategories(categoryIds);
        Map<String, String> categoryViewMap = categoryViews.stream().collect(Collectors.toMap(
                CategoryView::id,
                CategoryView::name
        ));

        return summaries.stream()
                .map(summary -> new ProductSummaryResult.Product(
                        summary.id(),
                        summary.name(),
                        summary.status(),
                        summary.slug(),
                        resolveCategoryName(categoryViewMap, summary.categoryId()),
                        new ProductSummaryResult.VariantSummary(
                                summary.variantSummary().available(),
                                extractVariantTypes(summary.variantSummary())
                        )
                ))
                .toList();
    }

    private List<ProductSummaryResult.VariantType> extractVariantTypes(ProductSummary.VariantSummary variantSummary) {
        return variantSummary.types().stream()
                .map(type -> new ProductSummaryResult.VariantType(
                        type.typeId(),
                        "",
                        type.options().stream().map(
                                option -> new ProductSummaryResult.VariantOption(
                                        option.optionId(),
                                        ""
                                )).toList()
                ))
                .toList();
    }

    @Override
    public Class<GetProductsByCategoryQuery> getQueryType() {
        return GetProductsByCategoryQuery.class;
    }

    private List<CategoryView> fetchCategories(List<String> categoryIds) {
        return categoryQueryRepository.findViewByIds(categoryIds);
    }

    private String resolveCategoryName(Map<String, String> categoryViewMap, String categoryId) {
        String name = categoryViewMap.get(categoryId);
        log.info("Resolving category name:{} by Id:{} ", name, categoryId);
        return name != null ? name : categoryId;
    }
}

package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductVariantQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.CategoryView;
import com.catalog.infrastructure.view.ProductVariantView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.ProductVariantSummaryQuery;
import com.grab.store.catalog.internal.query.ProductVariantSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductVariantSearchQueryHandler
        implements QueryHandler<ProductVariantSummaryQuery, Page<ProductVariantSummaryResult>> {

    private static final Logger log = Loggers.getLogger(ProductVariantSearchQueryHandler.class);

    private final ProductVariantQueryRepository productVariantQueryRepository;
    private final CategoryQueryRepository categoryRepository;

    @Override
    @CatalogReadTransactional
    public Page<ProductVariantSummaryResult> handle(ProductVariantSummaryQuery query) {
        log.debug("Handling ProductVariantSummaryQuery");

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(query.merchantId())
                .query(query.query())
                .variantStatus(query.variantStatus())
                .categoryId(query.categoryId())
                .productStatus(query.productStatus())
                .build();

        Page<ProductVariantView> page = productVariantQueryRepository.search(criteria, query.pageable());
        Map<String, String> categoryViewMap = getCategoryViewMap(page.getContent());

        return page.map(view -> mapToResult(view, categoryViewMap));
    }

    @Override
    public Class<ProductVariantSummaryQuery> getQueryType() {
        return ProductVariantSummaryQuery.class;
    }

    private Map<String, String> getCategoryViewMap(List<ProductVariantView> views) {
        List<String> categoryIds = views.stream()
                .map(ProductVariantView::categoryId)
                .distinct()
                .toList();
        return categoryRepository.findViewByIds(categoryIds).stream()
                .collect(Collectors.toMap(CategoryView::id, CategoryView::name));
    }

    private ProductVariantSummaryResult mapToResult(ProductVariantView view, Map<String, String> categoryViewMap) {
        return new ProductVariantSummaryResult(
                view.productId(),
                view.variantId(),
                view.sku(),
                view.productName(),
                view.status(),
                view.slug(),
                resolveCategoryName(categoryViewMap, view.categoryId()),
                view.categoryId()
        );
    }

    private String resolveCategoryName(Map<String, String> categoryViewMap, String categoryId) {
        String name = categoryViewMap.get(categoryId);
        log.info("Resolving category name:{} by Id:{} ", name, categoryId);
        return name != null ? name : categoryId;
    }
}

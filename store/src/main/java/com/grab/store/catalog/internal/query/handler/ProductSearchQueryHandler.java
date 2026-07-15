package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.CategoryView;
import com.catalog.infrastructure.view.ProductView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.ProductSearchQuery;
import com.grab.store.catalog.internal.query.ProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductSearchQueryHandler implements QueryHandler<ProductSearchQuery, Page<ProductSearchResult>> {

    private static final Logger log = Loggers.getLogger(ProductSearchQueryHandler.class);

    private final ProductQueryRepository productQueryRepository;
    private final CategoryQueryRepository categoryRepository;

    @Override
    @CatalogReadTransactional
    public Page<ProductSearchResult> handle(ProductSearchQuery query) {
        log.debug("Handling ProductSummaryQuery");

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .merchantId(query.merchantId())
                .query(query.query())
                .variantStatus(query.variantStatus())
                .categoryId(query.categoryId())
                .productStatus(query.productStatus())
                .build();

        Page<ProductView> page = productQueryRepository.search(criteria, query.pageable());
        Map<String, String> categoryViewMap = getCategoryViewMap(page.getContent());

        return page.map(view -> mapToResult(view, categoryViewMap));
    }

    @Override
    public Class<ProductSearchQuery> getQueryType() {
        return ProductSearchQuery.class;
    }

    private Map<String, String> getCategoryViewMap(List<ProductView> views) {
        List<String> categoryIds = views.stream()
                .map(ProductView::categoryId)
                .distinct()
                .toList();
        return categoryRepository.findViewByIds(categoryIds).stream()
                .collect(Collectors.toMap(CategoryView::id, CategoryView::name));
    }

    private ProductSearchResult mapToResult(ProductView view, Map<String, String> categoryViewMap) {
        return new ProductSearchResult(
                view.id(),
                view.name(),
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

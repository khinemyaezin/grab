package com.catalog.application.service;

import com.catalog.application.port.inbound.ProductVariantSearchUseCase;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.ProductVariantQueryPort;
import com.catalog.application.readmodel.ProductSearchCriteria;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.readmodel.ProductVariantView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.query.ProductVariantSummaryQuery;
import com.catalog.application.query.ProductVariantSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProductVariantSearchService implements ProductVariantSearchUseCase {

    private static final Logger log = Loggers.getLogger(ProductVariantSearchService.class);

    private final ProductVariantQueryPort productVariantQueryRepository;
    private final CategoryQueryPort categoryRepository;

        public Page<ProductVariantSummaryResult> execute(ProductVariantSummaryQuery query) {
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

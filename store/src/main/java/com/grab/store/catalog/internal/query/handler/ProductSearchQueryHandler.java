package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.CategoryView;
import com.catalog.infrastructure.view.ProductHeroMediaView;
import com.catalog.infrastructure.view.ProductPublicationView;
import com.catalog.infrastructure.view.ProductView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.storage.FileStoragePort;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.ProductSearchQuery;
import com.grab.store.catalog.internal.query.ProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductSearchQueryHandler implements QueryHandler<ProductSearchQuery, Page<ProductSearchResult>> {

    private static final Logger log = Loggers.getLogger(ProductSearchQueryHandler.class);

    private final ProductQueryRepository productQueryRepository;
    private final CategoryQueryRepository categoryRepository;
    private final FileStoragePort fileStoragePort;

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
        Map<String, ProductHeroMediaView> heroMediaByProductId = getHeroMediaByProductId(page.getContent());
        Map<String, List<ProductSearchResult.Publication>> publicationsByProductId =
                getPublicationsByProductId(page.getContent());

        return page.map(view -> mapToResult(view, categoryViewMap, heroMediaByProductId, publicationsByProductId));
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

    private Map<String, ProductHeroMediaView> getHeroMediaByProductId(List<ProductView> views) {
        List<String> productIds = views.stream()
                .map(ProductView::id)
                .toList();
        return productQueryRepository.findHeroMediasByProductIds(productIds).stream()
                .collect(Collectors.toMap(
                        ProductHeroMediaView::productId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private Map<String, List<ProductSearchResult.Publication>> getPublicationsByProductId(List<ProductView> views) {
        List<String> productIds = views.stream()
                .map(ProductView::id)
                .toList();
        return productQueryRepository.findPublicationsByProductIds(productIds).stream()
                .collect(Collectors.groupingBy(
                        ProductPublicationView::productId,
                        Collectors.mapping(
                                view -> new ProductSearchResult.Publication(view.salesChannelId()),
                                Collectors.toList()
                        )
                ));
    }

    private ProductSearchResult mapToResult(
            ProductView view,
            Map<String, String> categoryViewMap,
            Map<String, ProductHeroMediaView> heroMediaByProductId,
            Map<String, List<ProductSearchResult.Publication>> publicationsByProductId
    ) {
        return new ProductSearchResult(
                view.id(),
                view.name(),
                view.status(),
                view.slug(),
                resolveCategoryName(categoryViewMap, view.categoryId()),
                view.categoryId(),
                toThumbnail(heroMediaByProductId.get(view.id())),
                publicationsByProductId.getOrDefault(view.id(), List.of())
        );
    }

    private ProductSearchResult.Media toThumbnail(ProductHeroMediaView hero) {
        if (hero == null || hero.storageKey() == null || hero.storageKey().isBlank()) {
            return null;
        }
        return new ProductSearchResult.Media(
                hero.mediaId(),
                hero.storageKey(),
                fileStoragePort.resolvePublicUrl(hero.storageKey()),
                hero.contentType(),
                hero.rank()
        );
    }

    private String resolveCategoryName(Map<String, String> categoryViewMap, String categoryId) {
        String name = categoryViewMap.get(categoryId);
        log.info("Resolving category name:{} by Id:{} ", name, categoryId);
        return name != null ? name : categoryId;
    }
}

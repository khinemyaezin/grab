package com.catalog.application.service;

import com.catalog.application.port.inbound.ProductSearchUseCase;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.readmodel.ProductSearchCriteria;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.readmodel.ProductHeroMediaView;
import com.catalog.application.readmodel.ProductPublicationView;
import com.catalog.application.readmodel.ProductView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.storage.FileStoragePort;
import com.catalog.application.query.ProductSearchQuery;
import com.catalog.application.query.ProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProductSearchService implements ProductSearchUseCase {

    private static final Logger log = Loggers.getLogger(ProductSearchService.class);

    private final ProductQueryPort productQueryRepository;
    private final CategoryQueryPort categoryRepository;
    private final FileStoragePort fileStoragePort;

        public Page<ProductSearchResult> execute(ProductSearchQuery query) {
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
                        Collectors.collectingAndThen(
                                Collectors.mapping(
                                        ProductPublicationView::salesChannelId,
                                        Collectors.toCollection(LinkedHashSet::new)
                                ),
                                channelIds -> channelIds.stream()
                                        .map(ProductSearchResult.Publication::new)
                                        .toList()
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

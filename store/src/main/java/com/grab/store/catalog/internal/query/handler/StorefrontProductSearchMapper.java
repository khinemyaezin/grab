package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.CategoryView;
import com.catalog.infrastructure.view.ProductHeroMediaView;
import com.catalog.infrastructure.view.ProductVariantRefView;
import com.catalog.infrastructure.view.ProductView;
import com.grab.framework.storage.FileStoragePort;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.queries.StorefrontProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StorefrontProductSearchMapper {

    private final ProductQueryRepository productQueryRepository;
    private final CategoryQueryRepository categoryRepository;
    private final FileStoragePort fileStoragePort;

    @CatalogReadTransactional
    public Page<StorefrontProductSearchResult> search(ProductSearchCriteria criteria, Pageable pageable) {
        Page<ProductView> page = productQueryRepository.search(criteria, pageable);
        Map<String, String> categoryNames = categoryNames(page.getContent());
        Map<String, ProductHeroMediaView> heroes = heroes(page.getContent());
        Map<String, List<StorefrontProductSearchResult.VariantRef>> variants = variants(page.getContent());
        return page.map(view -> toResult(view, categoryNames, heroes, variants));
    }

    private Map<String, String> categoryNames(List<ProductView> views) {
        List<String> categoryIds = views.stream()
                .map(ProductView::categoryId)
                .distinct()
                .toList();
        return categoryRepository.findViewByIds(categoryIds).stream()
                .collect(Collectors.toMap(CategoryView::id, CategoryView::name));
    }

    private Map<String, ProductHeroMediaView> heroes(List<ProductView> views) {
        List<String> productIds = views.stream().map(ProductView::id).toList();
        return productQueryRepository.findHeroMediasByProductIds(productIds).stream()
                .collect(Collectors.toMap(
                        ProductHeroMediaView::productId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private Map<String, List<StorefrontProductSearchResult.VariantRef>> variants(List<ProductView> views) {
        List<String> productIds = views.stream().map(ProductView::id).toList();
        Map<String, List<StorefrontProductSearchResult.VariantRef>> byProduct = new LinkedHashMap<>();
        for (ProductVariantRefView variant : productQueryRepository.findActiveVariantsByProductIds(productIds)) {
            byProduct.computeIfAbsent(variant.productId(), ignored -> new ArrayList<>())
                    .add(new StorefrontProductSearchResult.VariantRef(variant.variantId(), variant.sku()));
        }
        return byProduct;
    }

    private StorefrontProductSearchResult toResult(
            ProductView view,
            Map<String, String> categoryNames,
            Map<String, ProductHeroMediaView> heroes,
            Map<String, List<StorefrontProductSearchResult.VariantRef>> variants
    ) {
        return new StorefrontProductSearchResult(
                view.id(),
                view.name(),
                view.slug(),
                categoryNames.getOrDefault(view.categoryId(), view.categoryId()),
                view.categoryId(),
                view.condition(),
                view.featured(),
                view.merchantId(),
                toThumbnail(heroes.get(view.id())),
                variants.getOrDefault(view.id(), List.of())
        );
    }

    private StorefrontProductSearchResult.Media toThumbnail(ProductHeroMediaView hero) {
        if (hero == null || hero.storageKey() == null || hero.storageKey().isBlank()) {
            return null;
        }
        return new StorefrontProductSearchResult.Media(
                hero.mediaId(),
                hero.storageKey(),
                fileStoragePort.resolvePublicUrl(hero.storageKey()),
                hero.contentType(),
                hero.rank()
        );
    }
}

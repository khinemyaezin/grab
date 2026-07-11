package com.catalog.domain.repository;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Product;

import java.util.Collection;
import java.util.Optional;

public interface ProductRepository {
    void save(Product product);

    void delete(Product product);

    Optional<Product> find(Id productId);

    Optional<Product> find(Id productId, Id merchantId);

    Optional<Product> findBySlug(String slug);

    boolean isSlugTaken(Id merchantId, String slug, String excludeProductUuid);

    boolean isSkuTaken(Id merchantId, String sku, String excludeVariantUuid);

    boolean existsByCategoryIds(Collection<Id> categoryIds);

    Optional<Integer> findMaxSlugSuffix(String baseSlug);
}

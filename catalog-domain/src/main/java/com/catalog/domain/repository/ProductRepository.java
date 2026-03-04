package com.catalog.domain.repository;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Product;

import java.util.Optional;

public interface ProductRepository{
    void save(Product product);
    void delete(Product product);
    Optional<Product> find(Id productId);
    Optional<Product> findBySlug(String slug);
    boolean isSlugTaken(String slug, String excludeProductUuid);
    Optional<Integer> findMaxSlugSuffix(String baseSlug);
}

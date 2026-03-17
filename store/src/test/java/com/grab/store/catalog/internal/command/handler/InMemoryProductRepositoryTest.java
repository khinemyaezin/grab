package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.id.Id;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepositoryTest implements ProductRepository {
    private final Map<String, Product> storage = new ConcurrentHashMap<>();
    private Product lastSaved;

    public void put(Product product) {
        storage.put(product.getId().getValue(), product);
    }

    Product getLastSaved() {
        return lastSaved;
    }

    @Override
    public void save(Product product) {
        lastSaved = product;
        storage.put(product.getId().getValue(), product);
    }

    @Override
    public void delete(Product product) {
        storage.remove(product.getId().getValue());
    }

    @Override
    public Optional<Product> find(Id productId) {
        return Optional.ofNullable(storage.get(productId.getValue()));
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return storage.values().stream()
                .filter(product -> slug.equals(product.getSlug()))
                .findFirst();
    }

    @Override
    public boolean isSlugTaken(String slug, String excludeProductUuid) {
        return storage.values().stream()
                .anyMatch(p -> slug.equals(p.getSlug())
                        && (excludeProductUuid == null || !p.getId().getValue().equals(excludeProductUuid)));
    }

    @Override
    public boolean isSkuTaken(String sku, String excludeVariantUuid) {
        return storage.values().stream()
                .flatMap(product -> product.getVariants().stream())
                .anyMatch(variant -> variant.getSku().equalsIgnoreCase(sku)
                        && (excludeVariantUuid == null || !variant.getId().getValue().equals(excludeVariantUuid)));
    }

    @Override
    public boolean existsByCategoryIds(Collection<Id> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return false;
        }

        return storage.values().stream()
                .map(Product::getCategoryId)
                .anyMatch(categoryId -> categoryIds.stream()
                        .anyMatch(requestedId -> requestedId != null
                                && requestedId.getValue().equals(categoryId.getValue())));
    }

    @Override
    public Optional<Integer> findMaxSlugSuffix(String baseSlug) {
        String prefix = baseSlug + "-";
        return storage.values().stream()
                .map(Product::getSlug)
                .filter(s -> s != null && s.startsWith(prefix))
                .map(s -> s.substring(prefix.length()))
                .filter(s -> s.matches("\\d+"))
                .map(Integer::parseInt)
                .max(Integer::compareTo);
    }
}

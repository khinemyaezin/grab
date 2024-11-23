package com.product.domain.repository.category;

import com.product.domain.entity.category.Category;

import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> find(String id);
    void deleteCascade(String uuid);
}

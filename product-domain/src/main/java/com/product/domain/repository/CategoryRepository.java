package com.product.domain.repository;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.category.Category;

import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> find(Id id);
    void deleteCascade(Id uuid);
}

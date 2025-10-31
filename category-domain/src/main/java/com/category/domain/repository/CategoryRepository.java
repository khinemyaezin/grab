package com.category.domain.repository;

import com.category.domain.aggregate.Category;
import com.grab.framework.id.Id;

import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> find(Id id);
    void deleteCascade(Id uuid);
}

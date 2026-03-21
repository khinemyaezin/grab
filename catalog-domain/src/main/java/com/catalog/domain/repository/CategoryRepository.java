package com.catalog.domain.repository;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Category;

import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> find(Id id);
}

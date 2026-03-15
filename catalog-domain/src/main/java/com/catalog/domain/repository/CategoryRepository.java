package com.catalog.domain.repository;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Category;

import java.util.Optional;
import java.util.Set;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> find(Id id);
    Set<Id> findSubtreeIds(Id id);
    void deleteCascade(Category category);
}

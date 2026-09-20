package com.catalog.domain.port.outbound;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Category;

import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> find(Id id);
}

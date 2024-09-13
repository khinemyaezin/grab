package com.coolstuff.ecommerce.grab.domain.product.repository.category;

import com.coolstuff.ecommerce.grab.domain.product.entity.category.Category;

import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findByUuid(String uuid);
}

package com.coolstuff.ecommerce.grab.infrastructure.product.repository.category;

import com.coolstuff.ecommerce.grab.domain.product.entity.category.Category;
import com.coolstuff.ecommerce.grab.domain.product.repository.category.CategoryRepository;

import java.util.Optional;

public class CategoryFacadeRepository implements CategoryRepository {
    private final CategoryEntityRepository categoryEntityRepository;

    public CategoryFacadeRepository(CategoryEntityRepository categoryEntityRepository) {
        this.categoryEntityRepository = categoryEntityRepository;
    }

    @Override
    public Optional<Category> findByUuid(String uuid) {

        return this.categoryEntityRepository.findByUuid(uuid)
                .map( e-> {
                    Category category = new Category();
                    category.setUuid(e.getUuid());
                    return category;
                });
    }
}

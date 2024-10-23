package com.product.domain.repository.category;

import com.product.domain.entity.category.CategoryComposite;
import com.product.domain.entity.category.ICategory;

import java.util.Optional;

public interface CategoryRepository {
    ICategory save(ICategory category);

    ICategory save(ICategory category, String parentId);

    Optional<ICategory> findBy(String id);

    void deleteCascade(String id);

    Optional<CategoryComposite> findImmediateChildrenOf(String uuid);

    Optional<CategoryComposite> findParentOf(String uuid);
}

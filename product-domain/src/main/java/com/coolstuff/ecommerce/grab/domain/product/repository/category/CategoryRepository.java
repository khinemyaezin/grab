package com.coolstuff.ecommerce.grab.domain.product.repository.category;

import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryComposite;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryLeaf;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.PersistableCategory;

import java.util.Optional;

public interface CategoryRepository {
    CategoryLeaf save(PersistableCategory category);

    CategoryLeaf save(PersistableCategory category, String parentId);

    Optional<CategoryLeaf> findByUuid(String id);

    void deleteCascade(String id);

    Optional<CategoryComposite> findImmediateChildrenOf(String uuid);

    Optional<CategoryComposite> findParentOf(String uuid) ;
}

package com.coolstuff.ecommerce.grab.domain.product.service.category;

import com.coolstuff.core.nestedset.model.NodeComponent;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryComposite;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryLeaf;

import java.util.Optional;

public interface CategoryInquiryUseCase {
    Optional<CategoryComposite> findImmediateSubordinatesOf(String uuid);

    Optional<CategoryComposite> findChildrenOf(String uuid);

    Optional<CategoryComposite> findParentOf(String uuid) ;
}

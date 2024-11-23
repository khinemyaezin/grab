package com.product.infrastructure.integration.category;

import com.nestedset.app.config.NodeComponentFactory;

import com.nestedset.library.model.NodeComponent;
import com.product.domain.entity.category.CategoryComposite;
import com.product.domain.entity.category.CategoryLeaf;
import com.product.infrastructure.entity.category.CategoryEntity;

public class CategoryComponentFactory implements NodeComponentFactory<CategoryEntity,Long> {

    @Override
    public NodeComponent<CategoryEntity> createCompositeNodeComponent(CategoryEntity categoryEntity) {
        return new CategoryComposite<>(categoryEntity);
    }

    @Override
    public NodeComponent<CategoryEntity> createLeafNodeComponent(CategoryEntity categoryEntity) {
        return new CategoryLeaf<>(categoryEntity);
    }
}

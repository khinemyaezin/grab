package com.category.infrastructure.factory;

import com.category.infrastructure.entity.CategoryEntity;
import com.nestedset.app.config.NodeComponentFactory;
import com.nestedset.library.model.NodeComponent;
import com.category.domain.aggregate.CategoryComposite;
import com.category.domain.aggregate.CategoryLeaf;

public class CategoryComponentFactory  implements NodeComponentFactory<CategoryEntity,Long> {

    @Override
    public NodeComponent<CategoryEntity> createCompositeNodeComponent(CategoryEntity categoryEntity) {
        return new CategoryComposite<>(categoryEntity);
    }

    @Override
    public NodeComponent<CategoryEntity> createLeafNodeComponent(CategoryEntity categoryEntity) {
        return new CategoryLeaf<>(categoryEntity);
    }
}
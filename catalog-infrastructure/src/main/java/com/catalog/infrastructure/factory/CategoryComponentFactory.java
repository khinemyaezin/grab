package com.catalog.infrastructure.factory;

import com.nestedset.app.config.NodeComponentFactory;
import com.nestedset.library.model.NodeComponent;
import com.catalog.infrastructure.view.CategoryComposite;
import com.catalog.infrastructure.view.CategoryLeaf;
import com.catalog.infrastructure.entity.entity.CategoryEntity;

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
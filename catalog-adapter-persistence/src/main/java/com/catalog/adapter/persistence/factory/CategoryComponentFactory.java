package com.catalog.adapter.persistence.factory;

import com.nestedset.app.config.NodeComponentFactory;
import com.nestedset.library.model.NodeComponent;
import com.catalog.adapter.persistence.category.CategoryComposite;
import com.catalog.adapter.persistence.category.CategoryLeaf;
import com.catalog.adapter.persistence.entity.CategoryEntity;

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
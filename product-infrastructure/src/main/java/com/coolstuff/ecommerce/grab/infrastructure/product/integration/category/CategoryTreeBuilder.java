package com.coolstuff.ecommerce.grab.infrastructure.product.integration.category;

import com.coolstuff.core.nestedset.model.NodeComponent;
import com.coolstuff.core.nestedset.service.AbstractTreeBuilder;
import com.coolstuff.core.nestedset.service.NodeComponentFactory;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.AbstractCategory;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.category.CategoryEntity;

public class CategoryTreeBuilder extends AbstractTreeBuilder {
    public CategoryTreeBuilder(NodeComponentFactory nodeComponentFactory) {
        super(nodeComponentFactory);
    }

    @Override
    public void merge(NodeComponent source, NodeComponent target) throws ClassCastException{
        CategoryEntity sourceCategory =(CategoryEntity) source;
        AbstractCategory targetCategory = (AbstractCategory) target;

        targetCategory.setLft(sourceCategory.getLft());
        targetCategory.setRgt(sourceCategory.getRgt());
        targetCategory.setDepth(sourceCategory.getDepth());
        targetCategory.setRoot(sourceCategory.isRoot());
    }
}

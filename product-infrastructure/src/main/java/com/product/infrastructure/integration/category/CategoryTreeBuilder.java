package com.product.infrastructure.integration.category;

import com.product.infrastructure.entity.category.CategoryEntity;
import com.nestedset.library.model.NestedSet;
import com.nestedset.library.model.NodeComponent;
import com.product.domain.entity.category.AbstractCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryTreeBuilder extends AbstractTreeBuilder {
    public CategoryTreeBuilder(NodeComponentFactory nodeComponentFactory) {
        super(nodeComponentFactory);
    }

    @Override
    public void merge(NestedSet source, NodeComponent target) throws ClassCastException {
        CategoryEntity sourceCategory = (CategoryEntity) source;
        AbstractCategory targetCategory = (AbstractCategory) target;

        targetCategory.setUuid(sourceCategory.getUuid());
        targetCategory.setName(sourceCategory.getName());
        targetCategory.setLft(sourceCategory.getLft());
        targetCategory.setRgt(sourceCategory.getRgt());
        targetCategory.setDepth(sourceCategory.getDepth());
    }
}

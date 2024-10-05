package com.coolstuff.ecommerce.grab.infrastructure.product.integration.category;

import com.coolstuff.core.nestedset.model.NodeComponent;
import com.coolstuff.core.nestedset.service.NodeComponentFactory;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryComposite;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryLeaf;

public class CategoryComponentFactory implements NodeComponentFactory {
    @Override
    public NodeComponent createCompositeNodeComponent() {
        return new CategoryComposite();
    }

    @Override
    public NodeComponent createLeafNodeComponent() {
        return new CategoryLeaf();
    }
}

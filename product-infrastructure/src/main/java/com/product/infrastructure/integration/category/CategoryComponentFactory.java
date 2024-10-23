package com.product.infrastructure.integration.category;

import com.nestedset.library.model.NodeComponent;
import com.product.domain.entity.category.CategoryComposite;
import com.product.domain.entity.category.CategoryLeaf;
import org.springframework.stereotype.Component;

@Component
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

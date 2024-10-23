package com.product.infrastructure.integration.category;

import com.nestedset.library.model.NodeComponent;

public interface NodeComponentFactory {
    NodeComponent createCompositeNodeComponent();
    NodeComponent createLeafNodeComponent();
}

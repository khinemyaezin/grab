package com.product.infrastructure.integration.category;

import com.nestedset.library.model.NestedSet;
import com.nestedset.library.model.NodeComponent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TreeBuilder {
    Optional<NodeComponent> buildTree(Collection<? extends NestedSet> nodeList );
    Optional<NodeComponent> buildTree(List<NestedSet> nodeList );
    List<NodeComponent> getLeafList(NodeComponent node);
}

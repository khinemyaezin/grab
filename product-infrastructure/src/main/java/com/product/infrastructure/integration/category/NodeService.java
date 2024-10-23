package com.product.infrastructure.integration.category;

import com.nestedset.library.model.NestedSet;

import java.util.List;
import java.util.Optional;

public interface NodeService<T extends NestedSet,ID> {
    T createNode(T entity);

    T createNode(T entity, ID parentId);

    Optional<T> readNode(ID id);

    T updateNode(ID id, T entity);

    void deleteNode(ID id);

    List<T> findImmediateChildren(ID nodeId);

    List<T> findParentOf(ID id);

}

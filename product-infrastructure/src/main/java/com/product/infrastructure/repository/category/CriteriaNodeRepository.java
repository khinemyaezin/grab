package com.product.infrastructure.repository.category;

import com.nestedset.library.model.NestedSet;

import java.util.List;

public interface CriteriaNodeRepository<T extends NestedSet,ID> {
    Integer getMaxRight();

   /* List<T> getChildren(Integer left, Integer right);*/

    void incrementLeftBoundaryAfter(Integer right);

    void incrementRightBoundaryAfter(Integer right);

    void decrementLeftBoundaryAfter(Integer right, Integer width);

    void decrementRightBoundaryAfter(Integer right, Integer width);

    void removeNodesInRange(Integer left, Integer right);

    List<T> getImmediateChildren(ID nodeId);

    List<T> getParentOf(ID id);
}

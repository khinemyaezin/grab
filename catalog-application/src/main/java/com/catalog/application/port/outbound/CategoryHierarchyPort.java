package com.catalog.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.Set;

public interface CategoryHierarchyPort {
    Set<Id> findSubtreeIds(Id id);

    void deleteSubtree(Id id);
}

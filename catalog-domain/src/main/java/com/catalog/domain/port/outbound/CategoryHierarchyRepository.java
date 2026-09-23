package com.catalog.domain.port.outbound;

import com.grab.framework.id.Id;

public interface CategoryHierarchyRepository {
    void deleteSubtree(Id id);
}

package com.catalog.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.Set;

public interface CategoryHierarchyQueryPort {
    Set<Id> findSubtreeIds(Id id);
}

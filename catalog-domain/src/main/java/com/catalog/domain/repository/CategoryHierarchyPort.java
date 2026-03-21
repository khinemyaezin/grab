package com.catalog.domain.repository;

import com.grab.framework.id.Id;

import java.util.Set;

/**
 * This is the domain hierarchy port adapter interface.
 */
public interface CategoryHierarchyPort {
    Set<Id> findSubtreeIds(Id id);

    void deleteSubtree(Id id);
}


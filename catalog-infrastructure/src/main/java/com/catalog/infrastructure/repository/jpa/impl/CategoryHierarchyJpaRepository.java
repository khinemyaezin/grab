package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.repository.jpa.CategoryHierarchyPort;
import com.catalog.infrastructure.repository.jpa.CategoryNodeRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor
public class CategoryHierarchyJpaRepository implements CategoryHierarchyPort {

    private static final Logger log = Loggers.getLogger(CategoryHierarchyJpaRepository.class);

    private final CategoryNodeRepository categoryNodeRepository;
    private final PersistenceExecutor executor;
    private final IdGenerator idGenerator;

    @Override
    public Set<Id> findSubtreeIds(Id id) {
        log.debug("Loading category subtree ids for id={}", id.getValue());
        return executor.query("Category", () -> {
            Set<Id> categoryIds = new LinkedHashSet<>();
            for (String categoryUuid : categoryNodeRepository.findSubtreeIds(id.getValue())) {
                categoryIds.add(idGenerator.convertIdFrom(categoryUuid));
            }
            return categoryIds;
        });
    }

    @Override
    public void deleteSubtree(Id id) {
        executor.command("Category", () -> {
            log.info("Cascade deleting category id={}", id.getValue());
            categoryNodeRepository.removeSubtree(id.getValue());
        });
    }
}


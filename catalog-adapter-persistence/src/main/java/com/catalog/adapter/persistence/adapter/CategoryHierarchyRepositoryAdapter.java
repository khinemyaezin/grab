package com.catalog.adapter.persistence.adapter;

import com.catalog.domain.port.outbound.CategoryHierarchyRepository;
import com.catalog.adapter.persistence.repository.CategoryNodeRepository;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CategoryHierarchyRepositoryAdapter implements CategoryHierarchyRepository {

    private static final Logger log = Loggers.getLogger(CategoryHierarchyRepositoryAdapter.class);

    private final CategoryNodeRepository categoryNodeRepository;
    private final PersistenceExecutor executor;

    @Override
    public void deleteSubtree(Id id) {
        executor.command("Category", () -> {
            log.info("Cascade deleting category id={}", id.getValue());
            categoryNodeRepository.removeSubtree(id.getValue());
        });
    }
}

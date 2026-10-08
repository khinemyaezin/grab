package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.repository.CategoryNodeRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CategoryHierarchyRepositoryAdapterTest {

    private CategoryNodeRepository categoryNodeRepository;
    private CategoryHierarchyRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        categoryNodeRepository = mock(CategoryNodeRepository.class);
        adapter = new CategoryHierarchyRepositoryAdapter(
                categoryNodeRepository,
                new CatalogPersistenceExecutor()
        );
    }

    @Test
    void deleteSubtree_validId_delegatesToTreeStore() {
        Id id = new CommonId("root-1");
        adapter.deleteSubtree(id);

        verify(categoryNodeRepository).removeSubtree("root-1");
    }
}

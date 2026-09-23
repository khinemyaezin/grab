package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.repository.CategoryNodeRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryHierarchyQueryAdapterTest {

    private CategoryNodeRepository categoryNodeRepository;
    private IdGenerator idGenerator;
    private CategoryHierarchyQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        categoryNodeRepository = mock(CategoryNodeRepository.class);
        idGenerator = mock(IdGenerator.class);
        adapter = new CategoryHierarchyQueryAdapter(
                categoryNodeRepository,
                new CatalogPersistenceExecutor(),
                idGenerator
        );
    }

    @Test
    void findSubtreeIds_validId_delegatesToTreeStoreAndMapsToIds() {
        Set<String> subtreeIds = new LinkedHashSet<>();
        subtreeIds.add("root-1");
        subtreeIds.add("child-1");

        when(categoryNodeRepository.findSubtreeIds("root-1")).thenReturn(subtreeIds);
        when(idGenerator.convertIdFrom("root-1")).thenReturn(id("root-1"));
        when(idGenerator.convertIdFrom("child-1")).thenReturn(id("child-1"));

        Set<Id> result = adapter.findSubtreeIds(id("root-1"));

        assertThat(result).extracting(Id::getValue).containsExactly("root-1", "child-1");
        verify(categoryNodeRepository).findSubtreeIds("root-1");
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}

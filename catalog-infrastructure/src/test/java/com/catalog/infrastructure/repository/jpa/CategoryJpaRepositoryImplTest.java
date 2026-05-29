package com.catalog.infrastructure.repository.jpa;

import com.catalog.domain.aggregate.Category;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.mapper.jpa.CategoryJpaAssembler;
import com.catalog.infrastructure.repository.jpa.impl.CategoryJpaRepositoryImpl;
import com.catalog.infrastructure.repository.jpa.impl.CatalogPersistenceExecutor;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryJpaRepositoryImplTest {

    private CategoryNodeRepository categoryNodeRepository;
    private CategoryJpaRepo categoryJpaRepo;
    private CategoryJpaAssembler categoryJpaAssembler;
    private DomainEventProducer domainEventProducer;
    private CatalogPersistenceExecutor executor;
    private CategoryJpaRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        categoryNodeRepository = mock(CategoryNodeRepository.class);
        categoryJpaRepo = mock(CategoryJpaRepo.class);
        categoryJpaAssembler = mock(CategoryJpaAssembler.class);
        domainEventProducer = mock(DomainEventProducer.class);
        executor = new CatalogPersistenceExecutor();
        repository = new CategoryJpaRepositoryImpl(
                categoryNodeRepository,
                categoryJpaRepo,
                categoryJpaAssembler,
                domainEventProducer,
                executor
        );
    }

    @Test
    void save_newRootDelegatesInsertToTreeStore() {
        Category category = Category.createRoot(id("root-1"), "Electronics");
        CategoryEntity entity = new CategoryEntity();

        when(categoryJpaRepo.findByUuid("root-1")).thenReturn(Optional.empty());
        when(categoryJpaAssembler.buildFullEntityGraph(category, null)).thenReturn(entity);

        repository.save(category);

        verify(categoryNodeRepository).insert(entity, null);
        verify(categoryJpaRepo, never()).save(entity);
    }

    @Test
    void save_newChildDelegatesInsertToTreeStoreWithParentId() {
        Category parent = Category.createRoot(id("parent-1"), "Parent");
        Category category = Category.createChild(id("child-1"), "Child", parent);
        CategoryEntity entity = new CategoryEntity();

        when(categoryJpaRepo.findByUuid("child-1")).thenReturn(Optional.empty());
        when(categoryJpaAssembler.buildFullEntityGraph(category, null)).thenReturn(entity);

        repository.save(category);

        verify(categoryNodeRepository).insert(entity, "parent-1");
    }

    @Test
    void save_existingCategoryMergesAndPersistsWithoutTreeInsert() {
        Category category = Category.createRoot(id("root-1"), "Electronics");
        CategoryEntity existingEntity = new CategoryEntity();
        CategoryEntity mergedEntity = new CategoryEntity();

        when(categoryJpaRepo.findByUuid("root-1")).thenReturn(Optional.of(existingEntity));
        when(categoryJpaAssembler.buildFullEntityGraph(category, existingEntity)).thenReturn(mergedEntity);

        repository.save(category);

        verify(categoryJpaRepo).save(mergedEntity);
        verify(categoryNodeRepository, never()).insert(mergedEntity, null);
    }

    @Test
    void find_resolvesParentThroughTreeStore() {
        CategoryEntity entity = new CategoryEntity();
        entity.setUuid("child-1");
        CategoryEntity parent = new CategoryEntity();
        parent.setUuid("parent-1");
        Category category = Category.createChild(id("child-1"), "Child", Category.createRoot(id("parent-1"), "Parent"));

        when(categoryJpaRepo.findByUuid("child-1")).thenReturn(Optional.of(entity));
        when(categoryNodeRepository.findParent(entity)).thenReturn(Optional.of(parent));
        when(categoryJpaAssembler.buildFullDomainAggregate(entity, parent)).thenReturn(category);

        Optional<Category> result = repository.find(id("child-1"));

        assertThat(result).contains(category);
        verify(categoryNodeRepository).findParent(entity);
    }

    private Id id(String value) {
        return new CommonId(value);
    }
}

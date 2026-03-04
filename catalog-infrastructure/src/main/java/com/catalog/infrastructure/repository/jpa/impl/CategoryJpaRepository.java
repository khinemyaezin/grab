package com.catalog.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.nestedset.app.NestedSetNodeRepository;
import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.grab.framework.event.DomainEventProducer;
import com.catalog.infrastructure.mapper.jpa.CategoryJpaAssembler;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class CategoryJpaRepository implements CategoryRepository {
    private final NestedSetNodeRepository<CategoryEntity,Long> nodeRepository;
    private final CategoryJpaRepo categoryJpaRepository;
    private final CategoryJpaAssembler categoryJpaAssembler;
    private final DomainEventProducer domainEventProducer;

    @Override
    public void save(Category category) {
        Optional<CategoryEntity> categoryEntity = categoryJpaRepository.findByUuid(category.getId().getValue());
        CategoryEntity entity;
        if(categoryEntity.isPresent()) {
            entity = categoryJpaAssembler.buildFullEntityGraph(category, categoryEntity.get());
            categoryJpaRepository.save(entity);
        } else {
            entity = categoryJpaAssembler.buildFullEntityGraph(category, null);

            if(category.getParentId().isEmpty()) {
                nodeRepository.insertAsFirstRoot(entity);
            } else {
                Optional<CategoryEntity> parentEntity = categoryJpaRepository.findByUuid(category.getParentId().get().getValue());
                if(parentEntity.isPresent()) {
                    nodeRepository.insertAsLastChildOf(entity, parentEntity.get());
                } else {
                    throw new IllegalStateException("Parent category not found for category: " + category.getId().getValue());
                }
            }
        }
        domainEventProducer.produce(
                category.getClass().getSimpleName(),
                category.getId().getValue(),
                category.pullEvents());

    }

    @Override
    public Optional<Category> find(Id id) {
        return categoryJpaRepository.findByUuid(id.getValue())
                .map(categoryEntity -> {
                    Optional<CategoryEntity> parent = nodeRepository.getParent(categoryEntity);
                    if(parent.isPresent()) {
                        return categoryJpaAssembler.buildFullDomainAggregate(categoryEntity, parent.get());
                    } else {
                        return categoryJpaAssembler.buildFullDomainAggregate(categoryEntity, null);
                    }
                });
    }

    @Override
    public void deleteCascade(Category category) {
        categoryJpaRepository.findByUuid(category.getId().getValue())
                .ifPresent(nodeRepository::removeSubtree);
    }
}

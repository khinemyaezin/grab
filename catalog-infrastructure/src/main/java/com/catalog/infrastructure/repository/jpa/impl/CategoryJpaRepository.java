package com.catalog.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.nestedset.app.NestedSetNodeRepository;
import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.exception.CatalogInfraError;
import com.catalog.infrastructure.exception.CatalogInfraException;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.catalog.infrastructure.mapper.jpa.CategoryJpaAssembler;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class CategoryJpaRepository implements CategoryRepository {
    private static final Logger log = Loggers.getLogger(CategoryJpaRepository.class);

    private final NestedSetNodeRepository<CategoryEntity,Long> nodeRepository;
    private final CategoryJpaRepo categoryJpaRepository;
    private final CategoryJpaAssembler categoryJpaAssembler;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public void save(Category category) {
        executor.command("Category", () -> {
            log.info("Persisting category id={}, name={}", category.getId().getValue(), category.getName());
            Optional<CategoryEntity> categoryEntity = categoryJpaRepository.findByUuid(category.getId().getValue());
            CategoryEntity entity;
            if (categoryEntity.isPresent()) {
                entity = categoryJpaAssembler.buildFullEntityGraph(category, categoryEntity.get());
                categoryJpaRepository.save(entity);
            } else {
                entity = categoryJpaAssembler.buildFullEntityGraph(category, null);

                if (category.getParentId().isEmpty()) {
                    nodeRepository.insertAsFirstRoot(entity);
                } else {
                    String parentId = category.getParentId().get().getValue();
                    Optional<CategoryEntity> parentEntity = categoryJpaRepository.findByUuid(parentId);
                    if (parentEntity.isPresent()) {
                        nodeRepository.insertAsLastChildOf(entity, parentEntity.get());
                    } else {
                        log.warn("Parent category not found for categoryId={}, parentId={}", category.getId().getValue(), parentId);
                        throw new CatalogInfraException(
                                new CatalogInfraError.PersistenceNotFound("Category", parentId),
                                "Parent category not found: " + parentId + "."
                        );
                    }
                }
            }
            domainEventProducer.produce(
                    category.getClass().getSimpleName(),
                    category.getId().getValue(),
                    category.pullEvents());
            log.info("Persisted category id={}", category.getId().getValue());
        });

    }

    @Override
    public Optional<Category> find(Id id) {
        log.debug("Loading category by id={}", id.getValue());
        return executor.query("Category", () -> categoryJpaRepository.findByUuid(id.getValue())
                .map(categoryEntity -> {
                    Optional<CategoryEntity> parent = nodeRepository.getParent(categoryEntity);
                    if (parent.isPresent()) {
                        return categoryJpaAssembler.buildFullDomainAggregate(categoryEntity, parent.get());
                    } else {
                        return categoryJpaAssembler.buildFullDomainAggregate(categoryEntity, null);
                    }
                }));
    }

    @Override
    public void deleteCascade(Category category) {
        executor.command("Category", () -> {
            log.info("Cascade deleting category id={}", category.getId().getValue());
            categoryJpaRepository.findByUuid(category.getId().getValue())
                    .ifPresent(nodeRepository::removeSubtree);
        });
    }
}

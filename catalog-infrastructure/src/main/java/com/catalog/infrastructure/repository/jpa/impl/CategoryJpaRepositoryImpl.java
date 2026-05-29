package com.catalog.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.catalog.infrastructure.mapper.jpa.CategoryJpaAssembler;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.catalog.infrastructure.repository.jpa.CategoryNodeRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class CategoryJpaRepositoryImpl implements CategoryRepository {
    private static final Logger log = Loggers.getLogger(CategoryJpaRepositoryImpl.class);

    private final CategoryNodeRepository categoryNodeRepository;
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
                String nullableParentId =  category.getParentId().map(Id::getValue).orElse(null);
                categoryNodeRepository.insert(entity, nullableParentId);
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
                .map(categoryEntity -> categoryJpaAssembler.buildFullDomainAggregate(
                        categoryEntity,
                        categoryNodeRepository.findParent(categoryEntity).orElse(null)
                ))
        );
    }
}

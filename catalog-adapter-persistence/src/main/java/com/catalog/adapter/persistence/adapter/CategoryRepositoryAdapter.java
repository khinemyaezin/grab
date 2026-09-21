package com.catalog.adapter.persistence.adapter;

import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Category;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.adapter.persistence.entity.CategoryEntity;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.catalog.adapter.persistence.mapper.CategoryJpaAssembler;
import com.catalog.adapter.persistence.repository.CategoryJpaRepo;
import com.catalog.adapter.persistence.repository.CategoryNodeRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {
    private static final Logger log = Loggers.getLogger(CategoryRepositoryAdapter.class);

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

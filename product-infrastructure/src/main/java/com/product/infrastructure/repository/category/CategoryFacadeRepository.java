package com.product.infrastructure.repository.category;

import com.product.domain.entity.category.Category;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.domain.repository.category.CategoryRepository;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.service.CategoryEntityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryFacadeRepository implements CategoryRepository {
    private final CategoryEntityService categoryEntityService;
    private final DomainEventProducer domainEventProducer;

    @Transactional
    @Override
    public void save(Category category) {
        CategoryEntity categoryEntity = this.categoryEntityService.findOrCreateCategory(category);
        this.categoryEntityService
                .find(category.getParentId())
                .ifPresentOrElse(
                        parent -> categoryEntityService.save(parent, categoryEntity),
                        () -> categoryEntityService.save(categoryEntity));

        domainEventProducer.produce(category.getEvents());
    }

    @Override
    public Optional<Category> find(String id) {
        return Optional.empty();
    }

    @Override
    public void deleteCascade(String uuid) {
        this.categoryEntityService.find(uuid)
                .ifPresent(this.categoryEntityService::deleteCascade);
    }
}

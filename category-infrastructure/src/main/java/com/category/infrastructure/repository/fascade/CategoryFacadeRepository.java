package com.category.infrastructure.repository.fascade;

import com.category.domain.aggregate.Category;
import com.category.domain.repository.CategoryRepository;
import com.category.infrastructure.entity.CategoryEntity;
import com.category.infrastructure.event.DomainEventProducer;
import com.category.infrastructure.service.CategoryService;
import com.grab.framework.id.Id;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryFacadeRepository implements CategoryRepository {
    private final CategoryService categoryService;
    private final DomainEventProducer domainEventProducer;

    @Override
    public void save(Category category) {
        CategoryEntity categoryEntity = this.categoryService.findOrBuildCategory(category);
        category.getParentId()
                .flatMap(parentId-> this.categoryService.find(parentId.getValue()))
                .ifPresentOrElse(
                        parent -> categoryService.save(parent, categoryEntity),
                        () -> categoryService.save(categoryEntity));
        domainEventProducer.produce(category.getEvents());
    }

    @Override
    public Optional<Category> find(Id id) {
        return Optional.empty();
    }

    @Override
    public void deleteCascade(Id uuid) {
        this.categoryService.find(uuid.getValue())
                .ifPresent(this.categoryService::deleteCascade);
    }
}

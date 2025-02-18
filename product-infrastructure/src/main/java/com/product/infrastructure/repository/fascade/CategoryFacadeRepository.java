package com.product.infrastructure.repository.fascade;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.category.Category;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.domain.repository.CategoryRepository;
import com.product.infrastructure.event.DomainEventProducer;
import com.product.infrastructure.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryFacadeRepository implements CategoryRepository {
    private final CategoryService categoryService;
    private final DomainEventProducer domainEventProducer;

    @Transactional
    @Override
    public void save(Category category) {
        CategoryEntity categoryEntity = this.categoryService.findOrBuildCategory(category);
        category.getParentId()
                .flatMap(parentId-> this.categoryService.find(parentId.getValue()))
                .ifPresentOrElse(
                        parent -> {
                            categoryService.save(parent, categoryEntity);
                        },
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

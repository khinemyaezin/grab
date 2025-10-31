package com.category.infrastructure.service.impl;

import com.category.domain.aggregate.Category;
import com.category.infrastructure.entity.CategoryEntity;
import com.category.infrastructure.factory.CategoryEntityFactory;
import com.category.infrastructure.mapper.CategoryEntityMapper;
import com.category.infrastructure.repository.jpa.CategoryJpaRepository;
import com.category.infrastructure.service.CategoryService;
import com.nestedset.app.NestedSetNodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final NestedSetNodeRepository<CategoryEntity, Long> nodeRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryEntityMapper categoryEntityMapper;
    private final CategoryEntityFactory categoryEntityFactory;

    @Override
    public Optional<CategoryEntity> find(String categoryUuid) {
        return categoryJpaRepository.findByUuid(categoryUuid);
    }

    @Override
    public CategoryEntity findOrBuildCategory(Category category) {
        return this.categoryJpaRepository.findByUuid(category.getId().getValue())
                .map(categoryEntity -> {
                    this.categoryEntityMapper.map(category, categoryEntity);
                    return categoryEntity;
                })
                .orElseGet(() -> categoryEntityFactory.create(category));
    }

    @Override
    public void save(CategoryEntity categoryEntity) {
        nodeRepository.insertAsFirstRoot(categoryEntity);
    }

    @Override
    public void save(CategoryEntity parentCategoryEntity, CategoryEntity categoryEntity) {
        nodeRepository.insertAsLastChildOf(categoryEntity, parentCategoryEntity);
    }

    @Override
    public void deleteCascade(CategoryEntity categoryEntity) {
        this.nodeRepository.removeSubtree(categoryEntity);
    }
}

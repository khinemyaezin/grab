package com.product.infrastructure.service.impl;

import com.nestedset.app.NestedSetNodeRepository;
import com.product.domain.entity.category.Category;
import com.product.domain.entity.product.Product;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.mapper.category.CategoryEntityMapper;
import com.product.infrastructure.repository.category.CategoryEntityRepository;
import com.product.infrastructure.service.CategoryEntityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryEntityServiceImpl implements CategoryEntityService {
    private final NestedSetNodeRepository<CategoryEntity, Long> nodeRepository;
    private final CategoryEntityRepository categoryEntityRepository;
    private final CategoryEntityMapper categoryEntityMapper;

    @Override
    public Optional<CategoryEntity> find(String categoryUuid) {
        return categoryEntityRepository.findByUuid(categoryUuid);
    }

    @Override
    public CategoryEntity findOrCreateCategory(Category category){
        return this.categoryEntityRepository.findByUuid(category.getId())
                .map(categoryEntity-> {
                    this.categoryEntityMapper.map(category, categoryEntity);
                    return categoryEntity;
                })
                .orElseGet(()-> createNewCategory(category));
    }

    public CategoryEntity createNewCategory(Category product) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntityMapper.map(product, categoryEntity);
        return categoryEntity;
    }

    @Transactional
    @Override
    public void save(CategoryEntity categoryEntity){
        nodeRepository.insertAsFirstRoot(categoryEntity);
    }

    @Transactional
    @Override
    public void save(CategoryEntity parentCategoryEntity, CategoryEntity categoryEntity) {
        nodeRepository.insertAsLastChildOf(categoryEntity,parentCategoryEntity);
    }

    @Override
    public void deleteCascade(CategoryEntity categoryEntity) {
        this.nodeRepository.removeSubtree(categoryEntity);
    }
}

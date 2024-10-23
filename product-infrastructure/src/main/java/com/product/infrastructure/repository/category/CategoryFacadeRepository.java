package com.product.infrastructure.repository.category;

import com.product.domain.entity.category.ICategory;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.integration.category.CategoryService;
import com.product.infrastructure.integration.category.TreeBuilder;
import com.product.infrastructure.mapper.category.CategoryLeafMapper;
import com.product.infrastructure.mapper.category.CategoryEntityMapper;
import com.product.domain.entity.category.CategoryComposite;
import com.product.domain.entity.category.CategoryLeaf;
import com.product.domain.repository.category.CategoryRepository;
import com.product.infrastructure.validator.PersistableCategoryValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryFacadeRepository implements CategoryRepository {
    private final CategoryService categoryService;
    private final CategoryEntityMapper categoryEntityMapper;
    private final CategoryLeafMapper categoryLeafMapper;
    private final TreeBuilder treeBuilder;
    private final PersistableCategoryValidator persistableCategoryValidator;

    @Override
    public CategoryLeaf save(ICategory category) {
        persistableCategoryValidator.validate(category);
        CategoryEntity categoryEntity = this.categoryEntityMapper.convert(category);
        categoryEntity = categoryService.create(categoryEntity);
        return this.categoryLeafMapper.convert(categoryEntity);
    }

    @Override
    public CategoryLeaf save(ICategory category, String parentId) {
        persistableCategoryValidator.validate(category);
        CategoryEntity categoryEntity = this.categoryEntityMapper.convert(category);
        categoryEntity = categoryService.create(categoryEntity, parentId);
        return this.categoryLeafMapper.convert(categoryEntity);
    }

    @Override
    public Optional<ICategory> findBy(String id) {
        return this.categoryService.findBy(id)
                .map(entity-> entity);
    }

    @Override
    public void deleteCascade(String uuid) {
        this.categoryService.deleteCascade(uuid);
    }

    @Override
    public Optional<CategoryComposite> findImmediateChildrenOf(String uuid) {
        List<CategoryEntity> categoryEntities = this.categoryService.findImmediateCategory(uuid);
        return this.treeBuilder.buildTree(categoryEntities)
                .map( nodeComponent -> (CategoryComposite) nodeComponent);
    }

    @Override
    public Optional<CategoryComposite> findParentOf(String uuid) {
        List<CategoryEntity> categoryEntities = this.categoryService.findParentCategoryOf(uuid);
        return this.treeBuilder.buildTree(categoryEntities)
                .map(this.treeBuilder::getLeafList)
                .flatMap(categoryComposite -> categoryComposite.stream().findFirst())
                .map( nodeComponent -> (CategoryComposite) nodeComponent);

    }
}

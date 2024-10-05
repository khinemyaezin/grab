package com.coolstuff.ecommerce.grab.infrastructure.product.repository.category;

import com.coolstuff.core.nestedset.service.TreeBuilder;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryComposite;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryLeaf;
import com.coolstuff.ecommerce.grab.domain.product.entity.category.PersistableCategory;
import com.coolstuff.ecommerce.grab.domain.product.repository.category.CategoryRepository;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.category.CategoryEntity;
import com.coolstuff.ecommerce.grab.infrastructure.product.integration.category.CategoryService;
import com.coolstuff.ecommerce.grab.infrastructure.product.mapper.category.CategoryLeafMapper;
import com.coolstuff.ecommerce.grab.infrastructure.product.mapper.category.PersistableCategoryMapper;

import java.util.List;
import java.util.Optional;

public class CategoryFacadeRepository implements CategoryRepository {
    private final CategoryService categoryService;
    private final PersistableCategoryMapper persistableCategoryMapper;
    private final CategoryLeafMapper categoryLeafMapper;
    private final TreeBuilder treeBuilder;

    public CategoryFacadeRepository(CategoryService categoryService, PersistableCategoryMapper persistableCategoryMapper, CategoryLeafMapper categoryLeafMapper, TreeBuilder treeBuilder) {
        this.categoryService = categoryService;
        this.persistableCategoryMapper = persistableCategoryMapper;
        this.categoryLeafMapper = categoryLeafMapper;
        this.treeBuilder = treeBuilder;
    }


    @Override
    public CategoryLeaf save(PersistableCategory category) {
        CategoryEntity categoryEntity = this.persistableCategoryMapper.convert(category);
        categoryEntity = categoryService.create(categoryEntity);
        return this.categoryLeafMapper.convert(categoryEntity);
    }

    @Override
    public CategoryLeaf save(PersistableCategory category, String parentId) {
        CategoryEntity categoryEntity = this.persistableCategoryMapper.convert(category);
        categoryEntity = categoryService.create(categoryEntity, parentId);
        return this.categoryLeafMapper.convert(categoryEntity);
    }

    @Override
    public Optional<CategoryLeaf> findByUuid(String id) {
        return this.categoryService.findBy(id)
                .map(this.categoryLeafMapper::convert);
    }

    @Override
    public void deleteCascade(String uuid) {
        this.categoryService.deleteCascade(uuid);

    }

    @Override
    public Optional<CategoryComposite> findImmediateChildrenOf(String uuid) {
        List<CategoryEntity> categoryEntities = this.categoryService.findImmediateCategory(uuid);
        return this.treeBuilder.buildTree(categoryEntities)
                .map(nodeComponent -> (CategoryComposite) nodeComponent);
    }

    @Override
    public Optional<CategoryComposite> findParentOf(String uuid) {
        List<CategoryEntity> categoryEntities = this.categoryService.findParentCategoryOf(uuid);
        return this.treeBuilder.buildTree(categoryEntities)
                .map(this.treeBuilder::getLeafList)
                .flatMap(categoryComposite -> categoryComposite.stream().findFirst())
                .map(nodeComponent -> (CategoryComposite) nodeComponent);

    }
}

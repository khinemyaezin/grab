package com.coolstuff.ecommerce.grab.infrastructure.product.integration.category;

import com.coolstuff.core.nestedset.service.AbstractNodeService;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.category.CategoryEntity;
import com.coolstuff.ecommerce.grab.infrastructure.product.repository.category.CategoryEntityRepository;

import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl extends AbstractNodeService<CategoryEntity,Long> implements CategoryService{
    private final CategoryEntityRepository categoryRepository;

    public CategoryServiceImpl(CategoryEntityRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
    }


    public CategoryEntity create(CategoryEntity entity){
       return super.createNode(entity);
    }

    public CategoryEntity create(CategoryEntity entity, String parentId){
        Long id = this.categoryRepository.findIdByUuid(parentId).orElseThrow();
        return super.createNode(entity,id);
    }

    public Optional<CategoryEntity> findBy(String id){
        return this.categoryRepository.findIdByUuid(id)
                .flatMap(this.categoryRepository::findById);
    }

    public CategoryEntity update(String uuid, CategoryEntity entity){
        Long id = this.categoryRepository.findIdByUuid(uuid).orElseThrow();
        return super.updateNode(id,entity);
    }

    public void deleteCascade(String uuid){
        Long id = this.categoryRepository.findIdByUuid(uuid).orElseThrow();
        super.deleteNode(id);
    }

    public List<CategoryEntity> findImmediateCategory(String nodeId){
        Long id = this.categoryRepository.findIdByUuid(nodeId).orElseThrow();
        return super.findImmediateChildren(id);
    }

    public List<CategoryEntity> findParentCategoryOf(String uuid){
        Long id = this.categoryRepository.findIdByUuid(uuid).orElseThrow();
        return super.findParentOf(id);
    }

}

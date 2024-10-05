package com.coolstuff.ecommerce.grab.domain.product.service.category;

import com.coolstuff.ecommerce.grab.domain.product.entity.category.CategoryComposite;
import com.coolstuff.ecommerce.grab.domain.product.repository.category.CategoryRepository;

import java.util.Optional;

public class CategoryInquiryUseCaseImpl implements CategoryInquiryUseCase{
    private final CategoryRepository categoryRepository;

    public CategoryInquiryUseCaseImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Optional<CategoryComposite> findImmediateSubordinatesOf(String uuid) {
        return this.categoryRepository.findImmediateChildrenOf(uuid);
    }

    @Override
    public Optional<CategoryComposite> findChildrenOf(String uuid) {
       return this.categoryRepository.findImmediateChildrenOf(uuid);
    }

    @Override
    public Optional<CategoryComposite> findParentOf(String uuid) {
        return this.categoryRepository.findParentOf(uuid);
    }
}

package com.grab.store.product.usecase.category;

import com.grab.store.product.annotation.UseCase;
import com.grab.store.product.exception.ProductError;
import com.grab.store.product.exception.ResourceNotFoundException;
import com.grab.store.product.mapper.category.ReadableCategoryMapper;
import com.grab.store_interface.product.dto.category.ReadableCategory;
import com.grab.store_interface.product.usecase.category.CategoryInquiryUseCase;
import com.product.domain.entity.category.CategoryComposite;
import com.product.domain.repository.category.CategoryRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.function.Supplier;

@UseCase
@AllArgsConstructor
public class CategoryInquiryUseCaseImpl implements CategoryInquiryUseCase {
    private final CategoryRepository categoryRepository;
    private final ReadableCategoryMapper readableCategoryMapper;

    @Override
    public Optional<ReadableCategory> findImmediateSubordinatesOf(String id) {
        return this.categoryRepository.findImmediateChildrenOf(id)
                .map(this.readableCategoryMapper::convert);
    }

    @Override
    public Optional<ReadableCategory> findChildrenOf(String id) {
        return this.categoryRepository.findImmediateChildrenOf(id)
                .map(this.readableCategoryMapper::convert);
    }

    @Override
    public Optional<ReadableCategory> findParentOf(String id) {
        return this.categoryRepository.findParentOf(id)
                .map(this.readableCategoryMapper::convert);
    }
}

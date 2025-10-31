package com.grab.store.product.internal.usecase.specification;

import com.grab.framework.id.Id;
import com.grab.framework.specification.CompositeSpecification;
import com.product.domain.repository.ProductRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NewProductSpec extends CompositeSpecification<Id> {
    private final ProductRepository productRepository;

    @Override
    public boolean isSatisfiedBy(Id s) {
        return !productRepository.exists(s);
    }
}

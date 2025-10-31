package com.grab.store.product.internal.usecase.specification;

import com.grab.framework.id.Id;
import com.grab.framework.specification.CompositeSpecification;
import com.grab.store.product.internal.command.CreateProductCommand;
import com.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateProductSpec extends CompositeSpecification<CreateProductCommand> {
    private final CompositeSpecification<Id> newProdSpec;

    public CreateProductSpec(ProductRepository productRepository) {
        this.newProdSpec = new NewProductSpec(productRepository);
    }

    @Override
    public boolean isSatisfiedBy(CreateProductCommand product) {
        return Objects.nonNull(product.id()) && newProdSpec.isSatisfiedBy(product.id());
    }
}

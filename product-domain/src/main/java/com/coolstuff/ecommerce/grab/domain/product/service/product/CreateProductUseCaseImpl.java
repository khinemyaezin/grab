package com.coolstuff.ecommerce.grab.domain.product.service.product;

import com.coolstuff.ecommerce.grab.domain.product.entity.product.PersistableProduct;
import com.coolstuff.ecommerce.grab.domain.product.entity.product.ReadableProduct;
import com.coolstuff.ecommerce.grab.domain.product.generic.Validator;
import com.coolstuff.ecommerce.grab.domain.product.repository.product.ProductRepository;

public class CreateProductUseCaseImpl implements CreateProductUseCase {
    private final ProductRepository productRepository;
    private final Validator<PersistableProduct> createProductValidator;

    public CreateProductUseCaseImpl(ProductRepository productRepository, Validator<PersistableProduct> createProductValidator) {
        this.productRepository = productRepository;
        this.createProductValidator = createProductValidator;
    }

    @Override
    public ReadableProduct createProduct(PersistableProduct persistableProduct) {
        this.createProductValidator.validate(persistableProduct);
        return this.productRepository.save(persistableProduct);
    }
}

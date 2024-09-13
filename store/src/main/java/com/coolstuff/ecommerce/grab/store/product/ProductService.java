package com.coolstuff.ecommerce.grab.store.product;


import com.coolstuff.ecommerce.grab.domain.product.service.product.CreateProductUseCase;

public class ProductService {
    private final CreateProductUseCase createProductUseCase;

    public ProductService(CreateProductUseCase createProductUseCase) {
        this.createProductUseCase = createProductUseCase;
    }
}

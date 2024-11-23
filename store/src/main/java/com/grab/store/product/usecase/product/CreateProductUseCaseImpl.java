package com.grab.store.product.usecase.product;

import com.grab.store.product.annotation.UseCase;
import com.grab.store_interface.product.dto.product.PersistableProduct;
import com.grab.store_interface.product.dto.product.ReadableProduct;
import com.grab.store.product.mapper.product.PersistableProductMapper;
import com.grab.store.product.mapper.product.ReadableProductMapper;
import com.grab.store_interface.product.usecase.product.CreateProductUseCase;
import com.product.domain.entity.product.Product;
import com.product.domain.generic.Validator;
import com.product.domain.repository.product.ProductRepository;
import lombok.AllArgsConstructor;

@UseCase
@AllArgsConstructor
public class CreateProductUseCaseImpl implements CreateProductUseCase {
 /*   private final ProductRepository productRepository;
    private final Validator<PersistableProduct> createProductValidator;
    private final PersistableProductMapper persistableProductMapper;
    private final ReadableProductMapper readableProductMapper;*/


    @Override
    public ReadableProduct createProduct(PersistableProduct persistableProduct) {
        /*this.createProductValidator.validate(persistableProduct);
        Product product = this.persistableProductMapper.convert(persistableProduct);
        product = this.productRepository.save(product);
        return this.readableProductMapper.convert(product);*/
        return null;
    }
}

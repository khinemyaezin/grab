package com.coolstuff.ecommerce.grab.domain.product.service.product;

import com.coolstuff.ecommerce.grab.domain.product.entity.product.PersistableProduct;
import com.coolstuff.ecommerce.grab.domain.product.entity.product.ReadableProduct;

public interface CreateProductUseCase {
    ReadableProduct createProduct(PersistableProduct persistableProduct);
}

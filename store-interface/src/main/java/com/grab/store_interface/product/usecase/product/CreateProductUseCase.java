package com.grab.store_interface.product.usecase.product;

import com.grab.store_interface.product.dto.product.PersistableProduct;
import com.grab.store_interface.product.dto.product.ReadableProduct;

public interface CreateProductUseCase {
    ReadableProduct createProduct(PersistableProduct persistableProduct);
}

package com.coolstuff.ecommerce.grab.domain.product.repository.product;

import com.coolstuff.ecommerce.grab.domain.product.entity.product.PersistableProduct;
import com.coolstuff.ecommerce.grab.domain.product.entity.product.Product;
import com.coolstuff.ecommerce.grab.domain.product.entity.product.ReadableProduct;

import java.util.Optional;

public interface ProductRepository {
    ReadableProduct save(PersistableProduct persistableProduct);
    void delete(String uuid);
    Optional<ReadableProduct> findByUuid(String uuid);
}

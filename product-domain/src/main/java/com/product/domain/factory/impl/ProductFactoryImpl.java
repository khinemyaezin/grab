package com.product.domain.factory.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.factory.ProductFactory;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class ProductFactoryImpl implements ProductFactory {
    private final IdGenerator idGenerator;

    public Product create(String name, Id categoryId) {
        Id productId = idGenerator.generateId();
        return Product.create(productId, name, categoryId);
    }
}

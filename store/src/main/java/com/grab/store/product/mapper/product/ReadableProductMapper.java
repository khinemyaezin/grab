package com.grab.store.product.mapper.product;

import com.grab.store.product.mapper.ObjectMapper;
import com.grab.store_interface.product.dto.product.ReadableProduct;
import com.product.domain.entity.product.Product;
import org.mapstruct.Mapper;

//@Mapper
public abstract class ReadableProductMapper implements ObjectMapper<Product, ReadableProduct> {
    public abstract ReadableProduct convert(Product source);
}

package com.grab.store.product.mapper.product;

import com.grab.store.product.mapper.ObjectMapper;
import com.grab.store_interface.product.dto.product.PersistableProduct;
import com.product.domain.entity.product.Product;
import org.mapstruct.Mapper;

//@Mapper
public abstract class PersistableProductMapper implements ObjectMapper<PersistableProduct, Product> {
    public abstract Product convert(PersistableProduct source);
}

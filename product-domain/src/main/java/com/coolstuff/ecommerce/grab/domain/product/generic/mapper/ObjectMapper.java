package com.coolstuff.ecommerce.grab.domain.product.generic.mapper;

public interface ObjectMapper<S,D> {
    D convert(S source);
}

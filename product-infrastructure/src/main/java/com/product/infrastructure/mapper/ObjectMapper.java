package com.product.infrastructure.mapper;

public interface ObjectMapper<S,D> {
    D convert(S source);
}

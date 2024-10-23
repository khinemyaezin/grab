package com.grab.store.product.mapper;

public interface ObjectMapper<S,D> {
    D convert(S source);
}

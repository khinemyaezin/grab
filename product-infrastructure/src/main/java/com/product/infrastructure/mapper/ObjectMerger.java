package com.product.infrastructure.mapper;

public interface ObjectMerger<S,D> {
    void merge(S source, D destination);
}

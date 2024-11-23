package com.product.domain.specification.framework;

public interface Specification<T> {
    boolean isSatisfiedBy(T t);
}

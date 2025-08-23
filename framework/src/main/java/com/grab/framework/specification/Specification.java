package com.grab.framework.specification;

public interface Specification<T> {
    boolean isSatisfiedBy(T t);
}

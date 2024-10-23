package com.product.domain.generic;

public interface Validator<T> {
    void validate(T input)  throws IllegalArgumentException ;
}

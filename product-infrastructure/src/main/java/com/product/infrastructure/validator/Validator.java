package com.product.infrastructure.validator;

public interface Validator<T> {
    void validate(T data);
}

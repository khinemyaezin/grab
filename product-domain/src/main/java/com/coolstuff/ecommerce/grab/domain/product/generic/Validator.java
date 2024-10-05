package com.coolstuff.ecommerce.grab.domain.product.generic;

public interface Validator<T> {
    void validate(T input)  throws IllegalArgumentException ;
}

package com.grab.store.product.internal.usecase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Response<T>{
    private final T result;
    private final boolean success;

    // Factory methods
    public static <T> Response<T> of(T result) {
        return new Response<>(result, true);
    }

    public static <T> Response<T> empty() {
        return new Response<>(null, true);
    }

}

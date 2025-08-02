package com.grab.store.product.internal.usecase;

public interface GenericUseCase<T,R> {
    R handle(T request);
}

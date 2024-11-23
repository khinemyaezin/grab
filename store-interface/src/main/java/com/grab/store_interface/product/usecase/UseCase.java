package com.grab.store_interface.product.usecase;

public interface UseCase<R,A> {
    R execute(A args);
}

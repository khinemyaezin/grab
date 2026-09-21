package com.cart.application.port.inbound;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;

public interface GetCurrentCartUseCase {
    CartResult execute(GetCurrentCartQuery query);
}

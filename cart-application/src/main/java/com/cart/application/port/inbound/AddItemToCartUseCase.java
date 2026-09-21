package com.cart.application.port.inbound;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;

public interface AddItemToCartUseCase {
    CartResult execute(AddItemToCartCommand command);
}

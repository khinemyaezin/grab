package com.grab.app.checkout.cart.internal.command.handler;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;
import com.cart.application.port.inbound.AddItemToCartUseCase;
import com.grab.app.checkout.cart.internal.config.CartTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class AddItemToCartCommandHandler implements CommandHandler<AddItemToCartCommand, CartResult> {

    private final AddItemToCartUseCase addItemToCartUseCase;

    public AddItemToCartCommandHandler(AddItemToCartUseCase addItemToCartUseCase) {
        this.addItemToCartUseCase = addItemToCartUseCase;
    }

    @Override
    @CartTransactional
    public CartResult handle(AddItemToCartCommand command) {
        return addItemToCartUseCase.execute(command);
    }

    @Override
    public Class<AddItemToCartCommand> getCommandType() {
        return AddItemToCartCommand.class;
    }
}

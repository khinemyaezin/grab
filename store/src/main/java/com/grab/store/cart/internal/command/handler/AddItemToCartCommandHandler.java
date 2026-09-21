package com.grab.store.cart.internal.command.handler;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;
import com.cart.application.port.inbound.AddItemToCartUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.cart.internal.config.CartTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddItemToCartCommandHandler implements CommandHandler<AddItemToCartCommand, CartResult> {
    private final AddItemToCartUseCase addItemToCartUseCase;

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

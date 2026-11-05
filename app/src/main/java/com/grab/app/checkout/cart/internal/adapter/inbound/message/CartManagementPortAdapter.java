package com.grab.app.checkout.cart.internal.adapter.inbound.message;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.cart.application.model.write.AddItemToCartCommand;
import com.grab.app.checkout.cart.internal.adapter.inbound.message.mapper.AddItemToCartPortMapper;
import com.grab.app.checkout.cart.internal.adapter.inbound.message.mapper.GetCurrentCartPortMapper;
import com.grab.app.checkout.cart.port.message.CartManagementPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CartManagementPortAdapter implements CartManagementPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final GetCurrentCartPortMapper getCurrentCartMapper;
    private final AddItemToCartPortMapper addItemToCartMapper;

    public CartManagementPortAdapter(
            @Qualifier("checkoutCommandBus") CommandBus commandBus,
            @Qualifier("checkoutQueryBus") QueryBus queryBus,
            GetCurrentCartPortMapper getCurrentCartMapper,
            AddItemToCartPortMapper addItemToCartMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.getCurrentCartMapper = getCurrentCartMapper;
        this.addItemToCartMapper = addItemToCartMapper;
    }

    @Override
    public CartMessageResult getCurrentCart(GetCurrentCartMessage message) {
        GetCurrentCartQuery query = getCurrentCartMapper.toQuery(message);
        CartResult result = queryBus.dispatch(query);
        return getCurrentCartMapper.toResponse(result);
    }

    @Override
    public CartMessageResult addItem(AddItemMessage message) {
        AddItemToCartCommand command = addItemToCartMapper.toCommand(message);
        CartResult result = commandBus.dispatch(command);
        return addItemToCartMapper.toResponse(result);
    }
}

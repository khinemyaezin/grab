package com.grab.service.checkout.cart.internal.adapter.inbound.message;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.cart.application.model.write.AddItemToCartCommand;
import com.grab.service.checkout.cart.internal.adapter.inbound.message.mapper.AddItemToCartPortMapper;
import com.grab.service.checkout.cart.internal.adapter.inbound.message.mapper.GetCurrentCartPortMapper;
import com.grab.service.contract.message.checkout.cart.CartManagementPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.grab.framework.exception.DomainException;

import java.rmi.RemoteException;
import com.grab.rpc.adapter.RmiExport;

@Component
@RmiExport
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
    public CartMessageResult getCurrentCart(GetCurrentCartMessage message) throws RemoteException {
        GetCurrentCartQuery query = getCurrentCartMapper.toQuery(message);
        try {
            CartResult result = queryBus.dispatch(query);
            return getCurrentCartMapper.toResponse(result);
        } catch (DomainException exception) {
            throw toRemoteException(exception);
        }
    }

    @Override
    public CartMessageResult addItem(AddItemMessage message) throws RemoteException {
        AddItemToCartCommand command = addItemToCartMapper.toCommand(message);
        try {
            CartResult result = commandBus.dispatch(command);
            return addItemToCartMapper.toResponse(result);
        } catch (DomainException exception) {
            throw toRemoteException(exception);
        }
    }
    private RemoteException toRemoteException(DomainException exception) {
        String code = exception.getMessageSource().code();
        return new RemoteException(code + ": " + exception.getMessage());
    }
}

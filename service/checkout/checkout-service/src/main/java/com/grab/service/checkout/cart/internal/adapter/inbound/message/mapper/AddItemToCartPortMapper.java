package com.grab.service.checkout.cart.internal.adapter.inbound.message.mapper;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.AddItemMessage;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.CartLineMessageResult;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.CartMessageResult;
import com.grab.service.checkout.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AddItemToCartPortMapper {

    public abstract AddItemToCartCommand toCommand(AddItemMessage message);

    public abstract CartMessageResult toResponse(CartResult result);

    protected abstract CartLineMessageResult toLineResponse(CartResult.CartLineResult line);
}


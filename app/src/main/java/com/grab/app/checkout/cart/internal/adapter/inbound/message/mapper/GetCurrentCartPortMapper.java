package com.grab.app.checkout.cart.internal.adapter.inbound.message.mapper;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.grab.app.checkout.cart.port.message.CartManagementPort.CartLineMessageResult;
import com.grab.app.checkout.cart.port.message.CartManagementPort.CartMessageResult;
import com.grab.app.checkout.cart.port.message.CartManagementPort.GetCurrentCartMessage;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCurrentCartPortMapper {

    public abstract GetCurrentCartQuery toQuery(GetCurrentCartMessage message);

    public abstract CartMessageResult toResponse(CartResult result);

    protected abstract CartLineMessageResult toLineResponse(CartResult.CartLineResult line);
}


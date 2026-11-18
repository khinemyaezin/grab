package com.grab.service.checkout.cart.internal.adapter.inbound.message.mapper;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.CartLineMessageResult;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.CartMessageResult;
import com.grab.service.contract.message.checkout.cart.CartManagementPort.GetCurrentCartMessage;
import com.grab.service.checkout.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCurrentCartPortMapper {

    public abstract GetCurrentCartQuery toQuery(GetCurrentCartMessage message);

    public abstract CartMessageResult toResponse(CartResult result);

    protected abstract CartLineMessageResult toLineResponse(CartResult.CartLineResult line);
}


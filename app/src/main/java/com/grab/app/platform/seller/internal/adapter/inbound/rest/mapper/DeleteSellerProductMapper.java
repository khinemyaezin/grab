package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.DeleteSellerProductCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeleteSellerProductMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "productId", source = "productId")
    public abstract DeleteSellerProductCommand toCommand(String merchantId, String productId);
}


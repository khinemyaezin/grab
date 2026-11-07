package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.ActivateSellerLocationCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class ActivateSellerLocationMapper {

    public abstract ActivateSellerLocationCommand toCommand(String locationId);
}


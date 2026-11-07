package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeactivateSellerLocationCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class DeactivateSellerLocationMapper {

    public abstract DeactivateSellerLocationCommand toCommand(String locationId);
}


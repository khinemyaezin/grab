package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.ActivateSellerZoneCommand;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class ActivateSellerZoneMapper {

    public abstract ActivateSellerZoneCommand toCommand(String zoneId);
}


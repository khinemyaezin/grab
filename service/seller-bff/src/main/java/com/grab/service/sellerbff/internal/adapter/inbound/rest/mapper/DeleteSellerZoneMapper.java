package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.DeleteSellerZoneCommand;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class DeleteSellerZoneMapper {

    public abstract DeleteSellerZoneCommand toCommand(String zoneId);
}


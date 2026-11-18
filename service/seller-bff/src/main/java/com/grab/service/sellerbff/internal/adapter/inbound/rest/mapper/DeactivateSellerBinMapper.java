package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.DeactivateSellerBinCommand;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class DeactivateSellerBinMapper {

    public abstract DeactivateSellerBinCommand toCommand(String binId);
}


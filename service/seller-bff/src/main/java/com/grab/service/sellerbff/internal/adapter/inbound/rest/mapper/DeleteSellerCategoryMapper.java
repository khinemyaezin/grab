package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.command.model.SellerCategoryCommands.DeleteSellerCategoryCommand;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class DeleteSellerCategoryMapper {

    public abstract DeleteSellerCategoryCommand toCommand(String categoryId);
}


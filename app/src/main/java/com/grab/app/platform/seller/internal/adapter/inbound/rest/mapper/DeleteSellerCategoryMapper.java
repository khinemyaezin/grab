package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.command.model.SellerCategoryCommands.DeleteSellerCategoryCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class DeleteSellerCategoryMapper {

    public abstract DeleteSellerCategoryCommand toCommand(String categoryId);
}


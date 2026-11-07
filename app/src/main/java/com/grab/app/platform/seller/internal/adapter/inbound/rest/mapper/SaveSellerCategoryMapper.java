package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SaveCategoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.command.model.SellerCategoryCommands.SaveSellerCategoryCommand;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class SaveSellerCategoryMapper {

    public abstract SaveSellerCategoryCommand toCommand(SaveCategoryRequest request);

    public abstract CategoryResponse toResponse(CategoryOutboundResult result);
}


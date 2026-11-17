package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.SaveCategoryRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.service.sellerbff.internal.command.model.SellerCategoryCommands.SaveSellerCategoryCommand;
import com.grab.service.sellerbff.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class SaveSellerCategoryMapper {

    public abstract SaveSellerCategoryCommand toCommand(SaveCategoryRequest request);

    public abstract CategoryResponse toResponse(CategoryOutboundResult result);
}


package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductStatusRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.UpdateSellerProductStatusCommand;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateSellerProductStatusMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "status", source = "request.status")
    public abstract UpdateSellerProductStatusCommand toCommand(String merchantId, String productId, UpdateProductStatusRequest request);

    public abstract ProductResponse toResponse(ProductOutboundResult result);

    public abstract VariantResponse toVariantResponse(VariantOutboundResult result);
}


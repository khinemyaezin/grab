package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.CreateSellerProductCommand;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.CreateVariantRecord;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateSellerProductMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "categoryId", source = "request.categoryId")
    @Mapping(target = "condition", source = "request.condition")
    @Mapping(target = "slug", source = "request.slug")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "variants", source = "request.variants")
    public abstract CreateSellerProductCommand toCommand(String merchantId, CreateProductRequest request);

    public abstract CreateVariantRecord toVariantRecord(CreateProductRequest.CreateVariantRequest request);

    public abstract ProductResponse toResponse(ProductOutboundResult result);

    public abstract VariantResponse toVariantResponse(VariantOutboundResult result);
}


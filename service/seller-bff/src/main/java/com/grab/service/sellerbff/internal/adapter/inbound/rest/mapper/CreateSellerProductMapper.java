package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.CreateProductRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.service.sellerbff.internal.command.model.SellerProductCommands.CreateSellerProductCommand;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.CreateVariantRecord;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateSellerProductMapper {

    public CreateSellerProductCommand toCommand(String merchantId, CreateProductRequest request) {
        if (request == null) {
            return null;
        }
        List<CreateVariantRecord> variants = request.resolveVariants() == null
                ? List.of()
                : request.resolveVariants().stream().map(this::toVariantRecord).toList();

        return new CreateSellerProductCommand(
                merchantId != null ? new CommonId(merchantId) : null,
                request.resolveName(),
                request.resolveCategoryId() != null ? new CommonId(request.resolveCategoryId()) : null,
                request.resolveCondition(),
                request.resolveSlug(),
                request.resolveStatus(),
                variants
        );
    }

    public abstract CreateVariantRecord toVariantRecord(CreateProductRequest.CreateVariantRequest request);

    public abstract ProductResponse toResponse(ProductOutboundResult result);

    public abstract VariantResponse toVariantResponse(VariantOutboundResult result);
}

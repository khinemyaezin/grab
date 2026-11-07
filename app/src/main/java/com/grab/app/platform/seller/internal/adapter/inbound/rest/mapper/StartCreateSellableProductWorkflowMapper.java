package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductRequest;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartCreateSellableProductWorkflowCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductMessage;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartCreateSellableProductWorkflowMapper {

    public StartCreateSellableProductWorkflowCommand toCommand(String merchantId, CreateSellableProductRequest request) {
        if (request == null) {
            return null;
        }
        CreateSellableProductMessage message = toMessage(merchantId, request);
        return new StartCreateSellableProductWorkflowCommand(message);
    }

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "merchantId")
    @Mapping(target = "scopeKey", constant = "merchant")
    @Mapping(target = "scopeId", source = "merchantId")
    @Mapping(target = "product", source = "request.product")
    @Mapping(target = "variantTypes", source = "request.variantTypes")
    @Mapping(target = "inventoryLines", source = "request.inventoryLines")
    @Mapping(target = "pricingLines", source = "request.pricingLines")
    @Mapping(target = "publicationLines", source = "request.publicationLines")
    @Mapping(target = "medias", source = "request.medias")
    @Mapping(target = "descriptions", source = "request.descriptions")
    @Mapping(target = "idempotencyKey", source = "request.idempotencyKey")
    public abstract CreateSellableProductMessage toMessage(String merchantId, CreateSellableProductRequest request);
}

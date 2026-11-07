package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductRequest;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductMessage;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartUpdateSellableProductWorkflowMapper {

    public StartUpdateSellableProductWorkflowCommand toCommand(String merchantId, UpdateSellableProductRequest request) {
        if (request == null) {
            return null;
        }
        UpdateSellableProductMessage message = toMessage(merchantId, request);
        return new StartUpdateSellableProductWorkflowCommand(message);
    }

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "merchantId")
    @Mapping(target = "scopeKey", constant = "merchant")
    @Mapping(target = "scopeId", source = "merchantId")
    @Mapping(target = "productId", source = "request.productId")
    @Mapping(target = "product", source = "request.product")
    @Mapping(target = "inventoryLines", source = "request.inventoryLines")
    @Mapping(target = "pricingLines", source = "request.pricingLines")
    @Mapping(target = "publicationLines", source = "request.publicationLines")
    @Mapping(target = "unpublishLines", source = "request.unpublishLines")
    @Mapping(target = "medias", source = "request.medias")
    @Mapping(target = "descriptions", source = "request.descriptions")
    @Mapping(target = "idempotencyKey", source = "request.idempotencyKey")
    public abstract UpdateSellableProductMessage toMessage(String merchantId, UpdateSellableProductRequest request);
}

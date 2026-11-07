package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantRequest;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantMessage;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartUpdateProductVariantWorkflowMapper {

    public StartUpdateProductVariantWorkflowCommand toCommand(String merchantId, UpdateProductVariantRequest request) {
        if (request == null) {
            return null;
        }
        UpdateProductVariantMessage message = toMessage(merchantId, request);
        return new StartUpdateProductVariantWorkflowCommand(message);
    }

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "merchantId")
    @Mapping(target = "scopeKey", constant = "merchant")
    @Mapping(target = "scopeId", source = "merchantId")
    @Mapping(target = "productId", source = "request.productId")
    @Mapping(target = "variantId", source = "request.variantId")
    @Mapping(target = "sku", source = "request.sku")
    @Mapping(target = "manageInventory", source = "request.manageInventory")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "inventoryLines", source = "request.inventoryLines")
    @Mapping(target = "idempotencyKey", source = "request.idempotencyKey")
    public abstract UpdateProductVariantMessage toMessage(String merchantId, UpdateProductVariantRequest request);
}

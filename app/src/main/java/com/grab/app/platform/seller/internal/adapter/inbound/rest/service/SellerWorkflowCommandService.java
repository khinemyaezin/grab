package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartCreateSellableProductWorkflowMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartUpdateProductVariantWorkflowMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartUpdateSellableProductWorkflowMapper;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartCreateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateSellableProductWorkflowCommand;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerWorkflowCommandService {

    private final CommandBus commandBus;
    private final StartCreateSellableProductWorkflowMapper startCreateWorkflowMapper;
    private final StartUpdateSellableProductWorkflowMapper startUpdateWorkflowMapper;
    private final StartUpdateProductVariantWorkflowMapper startUpdateVariantWorkflowMapper;

    public SellerWorkflowCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            StartCreateSellableProductWorkflowMapper startCreateWorkflowMapper,
            StartUpdateSellableProductWorkflowMapper startUpdateWorkflowMapper,
            StartUpdateProductVariantWorkflowMapper startUpdateVariantWorkflowMapper
    ) {
        this.commandBus = commandBus;
        this.startCreateWorkflowMapper = startCreateWorkflowMapper;
        this.startUpdateWorkflowMapper = startUpdateWorkflowMapper;
        this.startUpdateVariantWorkflowMapper = startUpdateVariantWorkflowMapper;
    }

    public CreateSellableProductResponse startCreateSellableProduct(String merchantId, CreateSellableProductRequest request) {
        StartCreateSellableProductWorkflowCommand cmd = startCreateWorkflowMapper.toCommand(merchantId, request);
        return commandBus.dispatch(cmd);
    }

    public UpdateSellableProductResponse startUpdateSellableProduct(String merchantId, UpdateSellableProductRequest request) {
        StartUpdateSellableProductWorkflowCommand cmd = startUpdateWorkflowMapper.toCommand(merchantId, request);
        return commandBus.dispatch(cmd);
    }

    public UpdateProductVariantResponse startUpdateProductVariant(String merchantId, UpdateProductVariantRequest request) {
        StartUpdateProductVariantWorkflowCommand cmd = startUpdateVariantWorkflowMapper.toCommand(merchantId, request);
        return commandBus.dispatch(cmd);
    }
}

package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ChangeSellerMerchantLifecycleMapper;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.*;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerMerchantAdminCommandService {

    private final CommandBus commandBus;
    private final ChangeSellerMerchantLifecycleMapper lifecycleMapper;

    public SellerMerchantAdminCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            ChangeSellerMerchantLifecycleMapper lifecycleMapper
    ) {
        this.commandBus = commandBus;
        this.lifecycleMapper = lifecycleMapper;
    }

    public MerchantResponse approve(String merchantId, String actorId) {
        MerchantOutboundResult result = commandBus.dispatch(new ApproveSellerMerchantCommand(merchantId, actorId));
        return lifecycleMapper.toResponse(result);
    }

    public MerchantResponse reject(String merchantId, String actorId, String reason) {
        MerchantOutboundResult result = commandBus.dispatch(new RejectSellerMerchantCommand(merchantId, actorId, reason));
        return lifecycleMapper.toResponse(result);
    }

    public MerchantResponse requestChanges(String merchantId, String actorId, String comments) {
        MerchantOutboundResult result = commandBus.dispatch(new RequestSellerMerchantChangesCommand(merchantId, actorId, comments));
        return lifecycleMapper.toResponse(result);
    }

    public MerchantResponse suspend(String merchantId, String actorId, String reason) {
        MerchantOutboundResult result = commandBus.dispatch(new SuspendSellerMerchantCommand(merchantId, actorId, reason));
        return lifecycleMapper.toResponse(result);
    }

    public MerchantResponse reactivate(String merchantId, String actorId) {
        MerchantOutboundResult result = commandBus.dispatch(new ReactivateSellerMerchantCommand(merchantId, actorId));
        return lifecycleMapper.toResponse(result);
    }

    public MerchantResponse close(String merchantId, String actorId) {
        MerchantOutboundResult result = commandBus.dispatch(new CloseSellerMerchantCommand(merchantId, actorId));
        return lifecycleMapper.toResponse(result);
    }
}


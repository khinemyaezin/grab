package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.MerchantLifecycleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.ChangeMerchantLifecycleRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.StartMerchantApplicationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.SubmitMerchantApplicationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.UpdateMerchantProfileRequestMapper;
import com.merchant.application.model.write.*;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand.Action;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
import com.merchant.domain.enums.MerchantType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantCommandService {
    private final CommandBus commands;
    private final StartMerchantApplicationRequestMapper startMapper;
    private final UpdateMerchantProfileRequestMapper updateMapper;
    private final SubmitMerchantApplicationRequestMapper submitMapper;
    private final ChangeMerchantLifecycleRequestMapper lifecycleMapper;

    public MerchantResponse start(StartMerchantApplicationRequest request, MerchantType type, String applicantId) {
        StartMerchantApplicationCommand command = startMapper.toCommand(applicantId, type, request);
        MerchantAccountResult result = commands.dispatch(command);
        return startMapper.toResponse(result);
    }

    public MerchantResponse update(String merchantId, UpdateMerchantProfileRequest request, String applicantId) {
        UpdateMerchantProfileCommand command = updateMapper.toCommand(merchantId, applicantId, request);
        MerchantAccountResult result = commands.dispatch(command);
        return updateMapper.toResponse(result);
    }

    public MerchantResponse submit(String merchantId, String applicantId) {
        SubmitMerchantApplicationCommand command = submitMapper.toCommand(merchantId, applicantId);
        MerchantAccountResult result = commands.dispatch(command);
        return submitMapper.toResponse(result);
    }

    public MerchantResponse changeLifecycle(
            String merchantId, String actorId, Action action, String reason
    ) {
        MerchantLifecycleRequest request = new MerchantLifecycleRequest(reason, null);
        ChangeMerchantLifecycleCommand command = lifecycleMapper.toCommand(merchantId, actorId, action, request);
        MerchantAccountResult result = commands.dispatch(command);
        return lifecycleMapper.toResponse(result);
    }
}

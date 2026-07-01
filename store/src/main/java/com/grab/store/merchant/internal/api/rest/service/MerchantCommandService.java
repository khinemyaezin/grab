package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.api.rest.mapper.ChangeMerchantLifecycleRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.StartMerchantApplicationRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.SubmitMerchantApplicationRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.UpdateMerchantProfileRequestMapper;
import com.grab.store.merchant.internal.command.*;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand.Action;
import com.grab.store.merchant.internal.config.MerchantEnabled;
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

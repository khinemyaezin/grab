package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.merchant.internal.api.rest.dto.request.CreateStorefrontRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.UpdateStorefrontProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.grab.store.merchant.internal.api.rest.mapper.ChangeStorefrontLifecycleRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.CreateStorefrontRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.UpdateStorefrontProfileRequestMapper;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand.Action;
import com.merchant.application.model.write.StorefrontResult;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@MerchantEnabled
@RequiredArgsConstructor
public class StorefrontCommandService {
    private final CommandBus commands;
    private final CreateStorefrontRequestMapper createMapper;
    private final UpdateStorefrontProfileRequestMapper updateMapper;
    private final ChangeStorefrontLifecycleRequestMapper lifecycleMapper;

    public StorefrontResponse create(String merchantId, CreateStorefrontRequest request) {
        StorefrontResult result = commands.dispatch(createMapper.toCommand(merchantId, request));
        return createMapper.toResponse(result);
    }

    public StorefrontResponse update(String storefrontId, String merchantId, UpdateStorefrontProfileRequest request) {
        StorefrontResult result = commands.dispatch(updateMapper.toCommand(storefrontId, merchantId, request));
        return updateMapper.toResponse(result);
    }

    public StorefrontResponse changeLifecycle(
            String storefrontId, String merchantId, Action action, MerchantLifecycleRequest request
    ) {
        StorefrontResult result = commands.dispatch(
                lifecycleMapper.toCommand(storefrontId, merchantId, action, request));
        return lifecycleMapper.toResponse(result);
    }
}

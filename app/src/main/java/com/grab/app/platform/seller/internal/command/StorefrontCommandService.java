package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.CreateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.MerchantLifecycleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.UpdateStorefrontProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.ChangeStorefrontLifecycleRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.CreateStorefrontRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.UpdateStorefrontProfileRequestMapper;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand.Action;
import com.merchant.application.model.write.StorefrontResult;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
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

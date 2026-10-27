package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response.CreatedPublishableKeyResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.mapper.PublishableKeyDtoMapper;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublishableKeyCommandService {
    private final CommandBus commandBus;
    private final PublishableKeyDtoMapper mapper;
    private final PublishableKeyAccessResolver accessResolver;

    public CreatedPublishableKeyResponse create(String salesChannelId, CreatePublishableKeyRequest request) {
        PublishableKeyManagementAccess access = accessResolver.resolve();
        com.saleschannel.application.model.write.CreatePublishableKeyCommand command =
                mapper.toCreateCommand(salesChannelId, request, access);
        CreatePublishableKeyResult result = commandBus.dispatch(command);
        return mapper.toCreatedResponse(result);
    }

    public void revoke(String salesChannelId, String keyId) {
        PublishableKeyManagementAccess access = accessResolver.resolve();
        com.saleschannel.application.model.write.RevokePublishableKeyCommand command =
                mapper.toRevokeCommand(salesChannelId, keyId, access);
        commandBus.dispatch(command);
    }
}

package com.grab.store.saleschannel.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.saleschannel.internal.api.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.store.saleschannel.internal.api.rest.dto.response.CreatedPublishableKeyResponse;
import com.grab.store.saleschannel.internal.api.rest.mapper.PublishableKeyDtoMapper;
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

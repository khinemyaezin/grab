package com.grab.service.channel.saleschannel.internal.adapter.inbound.message;

import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.CreatePublishableKeyPortMapper;
import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.ResolvePublishableKeyPortMapper;
import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.RevokePublishableKeyPortMapper;
import com.grab.service.contract.message.channel.saleschannel.PublishableApiKeyPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.saleschannel.application.model.read.ResolvePublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import com.saleschannel.application.model.write.RevokePublishableKeyCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Optional;

@Component
@RmiExport
public class PublishableApiKeyPortAdapter implements PublishableApiKeyPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreatePublishableKeyPortMapper createKeyMapper;
    private final RevokePublishableKeyPortMapper revokeKeyMapper;
    private final ResolvePublishableKeyPortMapper resolveKeyMapper;

    public PublishableApiKeyPortAdapter(
            @Qualifier("channelCommandBus") CommandBus commandBus,
            @Qualifier("channelQueryBus") QueryBus queryBus,
            CreatePublishableKeyPortMapper createKeyMapper,
            RevokePublishableKeyPortMapper revokeKeyMapper,
            ResolvePublishableKeyPortMapper resolveKeyMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createKeyMapper = createKeyMapper;
        this.revokeKeyMapper = revokeKeyMapper;
        this.resolveKeyMapper = resolveKeyMapper;
    }

    @Override
    public PublishableKeyResult createPublishableKey(CreatePublishableKeyMessage message) {
        CreatePublishableKeyCommand command = createKeyMapper.toCommand(message);
        CreatePublishableKeyResult result = commandBus.dispatch(command);
        return createKeyMapper.toResponse(result);
    }

    @Override
    public void revokePublishableKey(Id salesChannelId, Id keyId) {
        RevokePublishableKeyCommand command = revokeKeyMapper.toCommand(salesChannelId, keyId);
        commandBus.dispatch(command);
    }

    @Override
    public KeyResolutionResult resolveKey(String rawToken) {
        ResolvePublishableKeyQuery query = resolveKeyMapper.toQuery(rawToken);
        Optional<ResolvedPublishableKeyView> key = queryBus.dispatch(query);
        return key.map(resolveKeyMapper::toResponse).orElse(null);
    }
}

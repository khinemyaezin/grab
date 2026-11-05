package com.grab.app.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.PublishableApiKeyPort.CreatePublishableKeyMessage;
import com.grab.app.channel.saleschannel.port.message.PublishableApiKeyPort.PublishableKeyResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreatePublishableKeyPortMapper {

    public CreatePublishableKeyCommand toCommand(CreatePublishableKeyMessage message) {
        return message != null ? new CreatePublishableKeyCommand(message.salesChannelId(), message.label(), null) : null;
    }

    public abstract PublishableKeyResult toResponse(CreatePublishableKeyResult result);
}


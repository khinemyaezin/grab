package com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.service.channel.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.write.RevokePublishableKeyCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RevokePublishableKeyPortMapper {

    public RevokePublishableKeyCommand toCommand(Id salesChannelId, Id keyId) {
        return new RevokePublishableKeyCommand(salesChannelId, keyId, null);
    }
}


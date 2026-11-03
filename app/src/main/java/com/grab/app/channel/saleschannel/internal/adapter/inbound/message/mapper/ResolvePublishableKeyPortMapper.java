package com.grab.app.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.PublishableApiKeyPort.KeyResolutionResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.read.ResolvePublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ResolvePublishableKeyPortMapper {

    public ResolvePublishableKeyQuery toQuery(String rawToken) {
        return new ResolvePublishableKeyQuery(rawToken);
    }

    public abstract KeyResolutionResult toResponse(ResolvedPublishableKeyView view);
}


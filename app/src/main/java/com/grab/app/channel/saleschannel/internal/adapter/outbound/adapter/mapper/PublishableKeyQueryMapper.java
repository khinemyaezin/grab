package com.grab.app.channel.saleschannel.internal.adapter.outbound.mapper;

import com.grab.app.channel.saleschannel.port.PublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class PublishableKeyQueryMapper {
    @Mapping(target = "salesChannelId", expression = "java(view.salesChannelId() == null ? null : view.salesChannelId().getValue())")
    @Mapping(target = "channelEnabled", source = "channelEnabled")
    @Mapping(target = "keyActive", source = "keyActive")
    public abstract PublishableKeyQuery.Resolution toResolution(ResolvedPublishableKeyView view);
}

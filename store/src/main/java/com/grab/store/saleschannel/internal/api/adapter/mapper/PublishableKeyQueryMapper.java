package com.grab.store.saleschannel.internal.api.adapter.mapper;

import com.grab.store.saleschannel.port.PublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.grab.store.saleschannel.internal.api.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class PublishableKeyQueryMapper {
    @Mapping(target = "salesChannelId", expression = "java(view.salesChannelId() == null ? null : view.salesChannelId().getValue())")
    @Mapping(target = "channelEnabled", source = "channelEnabled")
    @Mapping(target = "keyActive", source = "keyActive")
    public abstract PublishableKeyQuery.Resolution toResolution(ResolvedPublishableKeyView view);
}

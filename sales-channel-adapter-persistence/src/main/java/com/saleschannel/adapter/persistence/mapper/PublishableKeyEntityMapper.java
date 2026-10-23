package com.saleschannel.adapter.persistence.mapper;

import com.grab.framework.mapper.IdMapper;
import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import com.saleschannel.adapter.persistence.entity.meta.PublishableKeyJpaEntity_;
import com.saleschannel.adapter.persistence.specification.jpa.PublishableKeyQuerySpecification.KeyMetadata;
import com.saleschannel.adapter.persistence.specification.jpa.PublishableKeyQuerySpecification.ChannelMetadata;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.domain.aggregate.PublishableKey;
import com.saleschannel.domain.enums.ChannelStatus;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class PublishableKeyEntityMapper {
    @Mapping(target = PublishableKeyJpaEntity_.ID, ignore = true)
    @Mapping(target = PublishableKeyJpaEntity_.UUID, source = "id")
    @Mapping(target = PublishableKeyJpaEntity_.SALES_CHANNEL_ID, source = "salesChannelId")
    @Mapping(target = PublishableKeyJpaEntity_.LABEL, source = "label")
    @Mapping(target = PublishableKeyJpaEntity_.TOKEN_DIGEST, source = "tokenDigest")
    @Mapping(target = PublishableKeyJpaEntity_.DISPLAY_PREFIX, source = "displayPrefix")
    @Mapping(target = PublishableKeyJpaEntity_.CREATED_AT, source = "createdAt")
    @Mapping(target = PublishableKeyJpaEntity_.REVOKED_AT, source = "revokedAt")
    @Mapping(target = PublishableKeyJpaEntity_.VERSION, ignore = true)
    public abstract PublishableKeyJpaEntity toEntity(PublishableKey source);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = PublishableKeyJpaEntity_.REVOKED_AT, source = "revokedAt")
    public abstract void update(PublishableKey source, @MappingTarget PublishableKeyJpaEntity destination);

    @Mapping(target = "id", source = PublishableKeyJpaEntity_.UUID)
    @Mapping(target = "salesChannelId", source = PublishableKeyJpaEntity_.SALES_CHANNEL_ID)
    @Mapping(target = "label", source = PublishableKeyJpaEntity_.LABEL)
    @Mapping(target = "tokenDigest", source = PublishableKeyJpaEntity_.TOKEN_DIGEST)
    @Mapping(target = "displayPrefix", source = PublishableKeyJpaEntity_.DISPLAY_PREFIX)
    @Mapping(target = "createdAt", source = PublishableKeyJpaEntity_.CREATED_AT)
    @Mapping(target = "revokedAt", source = PublishableKeyJpaEntity_.REVOKED_AT)
    public abstract PublishableKey toDomain(PublishableKeyJpaEntity source);

    public abstract PublishableKeyView toView(KeyMetadata source);

    @Mapping(target = "channelEnabled", source = "channelStatus")
    @Mapping(target = "keyActive", constant = "true")
    public abstract ResolvedPublishableKeyView toResolvedView(ChannelMetadata source);

    protected boolean enabled(ChannelStatus status) {
        return status == ChannelStatus.ENABLED;
    }
}

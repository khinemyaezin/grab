package com.saleschannel.adapter.persistence.entity.meta;

import com.saleschannel.adapter.persistence.entity.PublishableKeyJpaEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.Instant;

@StaticMetamodel(PublishableKeyJpaEntity.class)
public class PublishableKeyJpaEntity_ {
    public static volatile SingularAttribute<PublishableKeyJpaEntity, Long> id;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, String> uuid;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, String> salesChannelId;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, String> label;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, String> tokenDigest;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, String> displayPrefix;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, Instant> createdAt;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, Instant> revokedAt;
    public static volatile SingularAttribute<PublishableKeyJpaEntity, Long> version;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String SALES_CHANNEL_ID = "salesChannelId";
    public static final String LABEL = "label";
    public static final String TOKEN_DIGEST = "tokenDigest";
    public static final String DISPLAY_PREFIX = "displayPrefix";
    public static final String CREATED_AT = "createdAt";
    public static final String REVOKED_AT = "revokedAt";
    public static final String VERSION = "version";
}

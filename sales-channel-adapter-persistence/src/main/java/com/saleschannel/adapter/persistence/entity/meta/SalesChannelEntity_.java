package com.saleschannel.adapter.persistence.entity.meta;

import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.adapter.persistence.entity.ChannelRegionEntity;
import jakarta.persistence.metamodel.ListAttribute;

import java.time.Instant;

@StaticMetamodel(SalesChannelEntity.class)
public class SalesChannelEntity_ {
    public static volatile SingularAttribute<SalesChannelEntity, Long> id;
    public static volatile SingularAttribute<SalesChannelEntity, String> uuid;
    public static volatile SingularAttribute<SalesChannelEntity, String> name;
    public static volatile SingularAttribute<SalesChannelEntity, ChannelType> type;
    public static volatile SingularAttribute<SalesChannelEntity, ChannelOwner> owner;
    public static volatile SingularAttribute<SalesChannelEntity, String> merchantId;
    public static volatile SingularAttribute<SalesChannelEntity, ChannelStatus> status;
    public static volatile SingularAttribute<SalesChannelEntity, Instant> createdAt;
    public static volatile SingularAttribute<SalesChannelEntity, Instant> updatedAt;
    public static volatile SingularAttribute<SalesChannelEntity, Long> version;
    public static volatile ListAttribute<SalesChannelEntity, ChannelRegionEntity> supportedRegions;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String OWNER = "owner";
    public static final String MERCHANT_ID = "merchantId";
    public static final String STATUS = "status";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String VERSION = "version";
    public static final String SUPPORTED_REGIONS = "supportedRegions";
}

package com.inventory.infrastructure.entity.meta;

import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.domain.enums.ZoneType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ZoneEntity.class)
public class ZoneEntity_ {
    public static volatile SingularAttribute<ZoneEntity, Long> id;
    public static volatile SingularAttribute<ZoneEntity, String> uuid;
    public static volatile SingularAttribute<ZoneEntity, String> code;
    public static volatile SingularAttribute<ZoneEntity, String> name;
    public static volatile SingularAttribute<ZoneEntity, ZoneType> type;
    public static volatile SingularAttribute<ZoneEntity, Boolean> active;
    public static volatile SingularAttribute<ZoneEntity, String> locationId;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String ACTIVE = "active";
    public static final String LOCATION_ID = "locationId";
}

package com.inventory.infrastructure.entity.meta;

import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(BinEntity.class)
public class BinEntity_ {
    public static volatile SingularAttribute<BinEntity, Long> id;
    public static volatile SingularAttribute<BinEntity, String> uuid;
    public static volatile SingularAttribute<BinEntity, String> code;
    public static volatile SingularAttribute<BinEntity, String> name;
    public static volatile SingularAttribute<BinEntity, Integer> maxCapacity;
    public static volatile SingularAttribute<BinEntity, Boolean> active;
    public static volatile SingularAttribute<BinEntity, ZoneEntity> zone;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String MAX_CAPACITY = "maxCapacity";
    public static final String ACTIVE = "active";
    public static final String ZONE = "zone";
}

package com.inventory.infrastructure.entity.meta;

import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(LocationEntity.class)
public class LocationEntity_ {
    public static volatile SingularAttribute<LocationEntity, Long> id;
    public static volatile SingularAttribute<LocationEntity, String> uuid;
    public static volatile SingularAttribute<LocationEntity, String> code;
    public static volatile SingularAttribute<LocationEntity, String> name;
    public static volatile SingularAttribute<LocationEntity, LocationType> type;
    public static volatile SingularAttribute<LocationEntity, String> street;
    public static volatile SingularAttribute<LocationEntity, String> street2;
    public static volatile SingularAttribute<LocationEntity, String> city;
    public static volatile SingularAttribute<LocationEntity, String> state;
    public static volatile SingularAttribute<LocationEntity, String> postalCode;
    public static volatile SingularAttribute<LocationEntity, String> country;
    public static volatile SingularAttribute<LocationEntity, Boolean> active;
    public static volatile ListAttribute<LocationEntity, ZoneEntity> zones;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String STREET = "street";
    public static final String STREET2 = "street2";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String POSTAL_CODE = "postalCode";
    public static final String COUNTRY = "country";
    public static final String ACTIVE = "active";
    public static final String ZONES = "zones";
}

package com.inventory.domain.factory;

import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.valueobject.Address;

public interface LocationFactory {

    Location create(String code, String name, LocationType type, Address address);

    Location createWarehouse(String code, String name, Address address);

    Location createStore(String code, String name, Address address);
}

package com.inventory.domain.factory.impl;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.factory.LocationFactory;
import com.inventory.domain.valueobject.Address;

public class DefaultLocationFactory implements LocationFactory {

    private final IdGenerator idGenerator;

    public DefaultLocationFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Location create(String code, String name, LocationType type, Address address) {
        return new Location(idGenerator.generateId(), code, name, type, address);
    }

    @Override
    public Location createWarehouse(String code, String name, Address address) {
        return Location.createWarehouse(idGenerator.generateId(), code, name, address);
    }

    @Override
    public Location createStore(String code, String name, Address address) {
        return Location.createStore(idGenerator.generateId(), code, name, address);
    }
}

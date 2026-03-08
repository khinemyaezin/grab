package com.grab.store.inventory.internal.support;

import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Bin;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.query.GetLocationResult;

import java.util.List;

public final class LocationResultMapper {

    private LocationResultMapper() {
    }

    public static LocationResult toCommandResult(Location location) {
        return new LocationResult(
                location.getId().getValue(),
                location.getCode(),
                location.getName(),
                location.getType().name(),
                location.isActive(),
                toCommandAddress(location.getAddress()),
                toCommandZones(location.getZones())
        );
    }

    public static GetLocationResult toQueryResult(Location location) {
        return new GetLocationResult(
                location.getId().getValue(),
                location.getCode(),
                location.getName(),
                location.getType().name(),
                location.isActive(),
                toQueryAddress(location.getAddress()),
                toQueryZones(location.getZones())
        );
    }

    private static LocationResult.Address toCommandAddress(Address address) {
        if (address == null) {
            return null;
        }
        return new LocationResult.Address(
                address.line1(),
                address.line2(),
                address.city(),
                address.state(),
                address.postalCode(),
                address.country()
        );
    }

    private static GetLocationResult.Address toQueryAddress(Address address) {
        if (address == null) {
            return null;
        }
        return new GetLocationResult.Address(
                address.line1(),
                address.line2(),
                address.city(),
                address.state(),
                address.postalCode(),
                address.country()
        );
    }

    private static List<LocationResult.Zone> toCommandZones(List<Zone> zones) {
        return zones.stream()
                .map(zone -> new LocationResult.Zone(
                        zone.getId().getValue(),
                        zone.getCode(),
                        zone.getName(),
                        zone.getType().name(),
                        zone.isActive(),
                        toCommandBins(zone.getBins())
                ))
                .toList();
    }

    private static List<GetLocationResult.Zone> toQueryZones(List<Zone> zones) {
        return zones.stream()
                .map(zone -> new GetLocationResult.Zone(
                        zone.getId().getValue(),
                        zone.getCode(),
                        zone.getName(),
                        zone.getType().name(),
                        zone.isActive(),
                        toQueryBins(zone.getBins())
                ))
                .toList();
    }

    private static List<LocationResult.Bin> toCommandBins(List<Bin> bins) {
        return bins.stream()
                .map(bin -> new LocationResult.Bin(
                        bin.getId().getValue(),
                        bin.getCode(),
                        bin.getName(),
                        bin.getMaxCapacity(),
                        bin.isActive()
                ))
                .toList();
    }

    private static List<GetLocationResult.Bin> toQueryBins(List<Bin> bins) {
        return bins.stream()
                .map(bin -> new GetLocationResult.Bin(
                        bin.getId().getValue(),
                        bin.getCode(),
                        bin.getName(),
                        bin.getMaxCapacity(),
                        bin.isActive()
                ))
                .toList();
    }
}

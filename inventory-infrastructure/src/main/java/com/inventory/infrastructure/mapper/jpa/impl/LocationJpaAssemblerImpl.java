package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Bin;
import com.inventory.domain.entity.Zone;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.mapper.jpa.*;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocationJpaAssemblerImpl implements LocationJpaAssembler {
    private final LocationEntityMapper locationEntityMapper;
    private final ZoneEntityMapper zoneEntityMapper;
    private final BinEntityMapper binEntityMapper;
    private final LocationMapper locationMapper;
    private final ZoneMapper zoneMapper;
    private final BinMapper binMapper;

    @Override
    public LocationEntity buildFullEntityGraph(Location location, LocationEntity entity) {
        if (entity == null) {
            entity = new LocationEntity();
        }

        locationEntityMapper.toEntity(location, entity);
        syncZones(entity, location);
        return entity;
    }

    @Override
    public Location toFullDomainGraph(LocationEntity locationEntity) {
        Location location = locationMapper.toDomain(locationEntity);
        if (location != null && locationEntity.getZones() != null) {
            for (ZoneEntity zoneEntity : locationEntity.getZones()) {
                Zone zone = zoneMapper.toDomain(zoneEntity);
                if (zone != null && zoneEntity.getBins() != null) {
                    for (BinEntity binEntity : zoneEntity.getBins()) {
                        Bin bin = binMapper.toDomain(binEntity);
                        if (bin != null) {
                            zone.addBin(bin);
                        }
                    }
                }
                location.addZone(zone);
            }
        }
        return location;
    }

    private void syncZones(LocationEntity entity, Location domain) {
        entity.getZones().removeIf(zoneEntity ->
                domain.getZones().stream()
                        .noneMatch(z -> z.getId().getValue().equals(zoneEntity.getUuid()))
        );

        for (Zone zone : domain.getZones()) {
            ZoneEntity zoneEntity = entity.getZones().stream()
                    .filter(ze -> ze.getUuid().equals(zone.getId().getValue()))
                    .findFirst()
                    .orElseGet(() -> {
                        ZoneEntity newZone = new ZoneEntity();
                        entity.addZone(newZone);
                        return newZone;
                    });

            zoneEntityMapper.toEntity(zone, zoneEntity);
            syncBins(zoneEntity, zone);
        }
    }

    private void syncBins(ZoneEntity entity, Zone domain) {
        entity.getBins().removeIf(binEntity ->
                domain.getBins().stream()
                        .noneMatch(b -> b.getId().getValue().equals(binEntity.getUuid()))
        );

        for (Bin bin : domain.getBins()) {
            BinEntity binEntity = entity.getBins().stream()
                    .filter(be -> be.getUuid().equals(bin.getId().getValue()))
                    .findFirst()
                    .orElseGet(() -> {
                        BinEntity newBin = new BinEntity();
                        entity.addBin(newBin);
                        return newBin;
                    });

            binEntityMapper.toEntity(bin, binEntity);
        }
    }
}

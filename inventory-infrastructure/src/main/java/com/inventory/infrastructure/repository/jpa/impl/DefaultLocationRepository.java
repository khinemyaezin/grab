package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.Bin;
import com.inventory.domain.entity.Zone;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.mapper.jpa.BinMapper;
import com.inventory.infrastructure.mapper.jpa.LocationMapper;
import com.inventory.infrastructure.mapper.jpa.ZoneMapper;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultLocationRepository implements LocationRepository {

    private final LocationJpaRepository jpaRepository;
    private final LocationMapper locationMapper;
    private final ZoneMapper zoneMapper;
    private final BinMapper binMapper;

    @Override
    public Optional<Location> findById(Id id) {
        return jpaRepository.findByUuid(id.getValue())
                .map(this::toDomainWithZones);
    }

    @Override
    public Optional<Location> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(this::toDomainWithZones);
    }

    @Override
    public List<Location> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainWithZones)
                .toList();
    }

    @Override
    public List<Location> findAllActive() {
        return jpaRepository.findAllByActiveTrue().stream()
                .map(this::toDomainWithZones)
                .toList();
    }

    @Override
    public List<Location> findByType(LocationType type) {
        return jpaRepository.findAllByType(type).stream()
                .map(this::toDomainWithZones)
                .toList();
    }

    @Override
    public Location save(Location location) {
        LocationEntity entity = jpaRepository.findByUuid(location.getId().getValue())
                .orElse(new LocationEntity());

        updateEntityFromDomain(entity, location);
        LocationEntity saved = jpaRepository.save(entity);
        return toDomainWithZones(saved);
    }

    @Override
    public void delete(Id id) {
        jpaRepository.findByUuid(id.getValue())
                .ifPresent(jpaRepository::delete);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    private Location toDomainWithZones(LocationEntity entity) {
        Location location = locationMapper.toDomain(entity);
        if (location != null && entity.getZones() != null) {
            for (ZoneEntity zoneEntity : entity.getZones()) {
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

    private void updateEntityFromDomain(LocationEntity entity, Location domain) {
        entity.setUuid(domain.getId().getValue());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setActive(domain.isActive());

        if (domain.getAddress() != null) {
            entity.setStreet(domain.getAddress().line1());
            entity.setCity(domain.getAddress().city());
            entity.setState(domain.getAddress().state());
            entity.setPostalCode(domain.getAddress().postalCode());
            entity.setCountry(domain.getAddress().country());
        }

        syncZones(entity, domain);
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

            updateZoneEntity(zoneEntity, zone);
        }
    }

    private void updateZoneEntity(ZoneEntity entity, Zone domain) {
        entity.setUuid(domain.getId().getValue());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setActive(domain.isActive());

        syncBins(entity, domain);
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

            updateBinEntity(binEntity, bin);
        }
    }

    private void updateBinEntity(BinEntity entity, Bin domain) {
        entity.setUuid(domain.getId().getValue());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setMaxCapacity(domain.getMaxCapacity());
        entity.setActive(domain.isActive());
    }
}

package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.ZoneView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ZoneJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private ZoneJpaRepository zoneJpaRepository;

    private ZoneEntity activePickingZone;
    private ZoneEntity activeStorageZone;
    private ZoneEntity inactiveStagingZone;

    @BeforeEach
    void setUp() {
        zoneJpaRepository.deleteAll();

        activePickingZone = new ZoneEntity();
        activePickingZone.setUuid("uuid-zone-1");
        activePickingZone.setCode("ZONE-P1");
        activePickingZone.setName("Picking Zone A");
        activePickingZone.setType(ZoneType.PICKING);
        activePickingZone.setActive(true);
        activePickingZone.setLocationId("loc-1");

        activeStorageZone = new ZoneEntity();
        activeStorageZone.setUuid("uuid-zone-2");
        activeStorageZone.setCode("ZONE-S1");
        activeStorageZone.setName("Storage Zone B");
        activeStorageZone.setType(ZoneType.STORAGE);
        activeStorageZone.setActive(true);
        activeStorageZone.setLocationId("loc-1");

        inactiveStagingZone = new ZoneEntity();
        inactiveStagingZone.setUuid("uuid-zone-3");
        inactiveStagingZone.setCode("ZONE-ST1");
        inactiveStagingZone.setName("Staging Zone C");
        inactiveStagingZone.setType(ZoneType.STAGING);
        inactiveStagingZone.setActive(false);
        inactiveStagingZone.setLocationId("loc-1");

        zoneJpaRepository.saveAll(List.of(activePickingZone, activeStorageZone, inactiveStagingZone));
    }

    @Test
    void findByUuid_returnsEntity_whenExists() {
        Optional<ZoneEntity> result = zoneJpaRepository.findByUuid("uuid-zone-1");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("ZONE-P1");
        assertThat(result.get().getName()).isEqualTo("Picking Zone A");
    }

    @Test
    void findByUuid_returnsEmpty_whenNotExists() {
        Optional<ZoneEntity> result = zoneJpaRepository.findByUuid("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByLocationIdAndActive_returnsActiveZones() {
        List<ZoneEntity> result = zoneJpaRepository.findAllByLocationIdAndActive("loc-1", true);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ZoneEntity::getCode)
                .containsExactlyInAnyOrder("ZONE-P1", "ZONE-S1");
    }

    @Test
    void findAllByLocationIdAndActive_returnsInactiveZones() {
        List<ZoneEntity> result = zoneJpaRepository.findAllByLocationIdAndActive("loc-1", false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("ZONE-ST1");
    }

    @Test
    void findAllByLocationIdAndActive_returnsEmptyForUnknownLocation() {
        List<ZoneEntity> result = zoneJpaRepository.findAllByLocationIdAndActive("unknown-loc", true);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByCodeAndLocationId_returnsTrue_whenExists() {
        boolean result = zoneJpaRepository.existsByCodeAndLocationId("ZONE-P1", "loc-1");

        assertThat(result).isTrue();
    }

    @Test
    void existsByCodeAndLocationId_returnsFalse_whenCodeMismatch() {
        boolean result = zoneJpaRepository.existsByCodeAndLocationId("ZONE-P1", "loc-2");

        assertThat(result).isFalse();
    }

    @Test
    void existsByCodeAndLocationId_returnsFalse_whenNotExists() {
        boolean result = zoneJpaRepository.existsByCodeAndLocationId("NON-EXISTENT", "loc-1");

        assertThat(result).isFalse();
    }

    @Test
    void findAllByLocationId_returnsAllZonesForLocation() {
        Page<ZoneView> result = zoneJpaRepository.findAllByLocationId("loc-1", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(ZoneView::code)
                .containsExactlyInAnyOrder("ZONE-P1", "ZONE-S1", "ZONE-ST1");
    }

    @Test
    void findAllByLocationId_returnsEmptyForUnknownLocation() {
        Page<ZoneView> result = zoneJpaRepository.findAllByLocationId("unknown-loc", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByLocationIdAndActive_pageable_returnsActiveZones() {
        Page<ZoneView> result = zoneJpaRepository.findAllByLocationIdAndActive("loc-1", true, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(ZoneView::code)
                .containsExactlyInAnyOrder("ZONE-P1", "ZONE-S1");
        assertThat(result.getContent()).allMatch(ZoneView::active);
    }

    @Test
    void findAllByLocationIdAndActive_pageable_returnsInactiveZones() {
        Page<ZoneView> result = zoneJpaRepository.findAllByLocationIdAndActive("loc-1", false, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("ZONE-ST1");
        assertThat(result.getContent()).noneMatch(ZoneView::active);
    }

    @Test
    void findAllByType_returnsZonesMatchingType() {
        Page<ZoneView> result = zoneJpaRepository.findAllByType(ZoneType.PICKING, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("ZONE-P1");
        assertThat(result.getContent()).allMatch(v -> v.type() == ZoneType.PICKING);
    }

    @Test
    void findAllByType_returnsEmptyWhenNoMatch() {
        Page<ZoneView> result = zoneJpaRepository.findAllByType(ZoneType.RETURNS, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByActive_returnsActiveZones() {
        Page<ZoneView> result = zoneJpaRepository.findAllByActive(true, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(ZoneView::active);
    }

    @Test
    void findAllByActive_returnsInactiveZones() {
        Page<ZoneView> result = zoneJpaRepository.findAllByActive(false, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).noneMatch(ZoneView::active);
    }

    @Test
    void findAllByLocationId_respectsPagination() {
        Page<ZoneView> page0 = zoneJpaRepository.findAllByLocationId("loc-1", PageRequest.of(0, 2));
        Page<ZoneView> page1 = zoneJpaRepository.findAllByLocationId("loc-1", PageRequest.of(1, 2));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page1.getContent()).hasSize(1);
    }
}

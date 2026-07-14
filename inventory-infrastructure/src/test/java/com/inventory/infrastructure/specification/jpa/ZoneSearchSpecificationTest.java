package com.inventory.infrastructure.specification.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.ZoneJpaRepository;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.ZoneView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ZoneSearchSpecificationTest extends RepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ZoneJpaRepository zoneJpaRepository;

    @Autowired
    private LocationJpaRepository locationJpaRepository;

    private ZoneSearchSpecification specification;

    private ZoneEntity activePickingZone;
    private ZoneEntity activeStorageZone;
    private ZoneEntity inactiveStorageZone;

    @BeforeEach
    void setUp() {
        zoneJpaRepository.deleteAll();
        locationJpaRepository.deleteAll();
        specification = new ZoneSearchSpecification(entityManager);

        LocationEntity location1 = new LocationEntity();
        location1.setUuid("loc-uuid-1");
        location1.setCode("LOC-1");
        location1.setName("Loc 1");
        location1.setMerchantId("merch-1");
        location1.setType(LocationType.WAREHOUSE);
        location1.setActive(true);

        LocationEntity location2 = new LocationEntity();
        location2.setUuid("loc-uuid-2");
        location2.setCode("LOC-2");
        location2.setName("Loc 2");
        location2.setMerchantId("merch-2");
        location2.setType(LocationType.WAREHOUSE);
        location2.setActive(true);

        locationJpaRepository.saveAll(List.of(location1, location2));

        activePickingZone = new ZoneEntity();
        activePickingZone.setUuid("zone-uuid-1");
        activePickingZone.setCode("ZON-1");
        activePickingZone.setName("Picking A");
        activePickingZone.setType(ZoneType.PICKING);
        activePickingZone.setActive(true);
        activePickingZone.setLocationId("loc-uuid-1");

        activeStorageZone = new ZoneEntity();
        activeStorageZone.setUuid("zone-uuid-2");
        activeStorageZone.setCode("ZON-2");
        activeStorageZone.setName("Storage B");
        activeStorageZone.setType(ZoneType.STORAGE);
        activeStorageZone.setActive(true);
        activeStorageZone.setLocationId("loc-uuid-1");

        inactiveStorageZone = new ZoneEntity();
        inactiveStorageZone.setUuid("zone-uuid-3");
        inactiveStorageZone.setCode("ZON-3");
        inactiveStorageZone.setName("Storage C");
        inactiveStorageZone.setType(ZoneType.STORAGE);
        inactiveStorageZone.setActive(false);
        inactiveStorageZone.setLocationId("loc-uuid-2");

        zoneJpaRepository.saveAll(List.of(activePickingZone, activeStorageZone, inactiveStorageZone));
    }

    @Test
    void search_withEmptyCriteria_returnsAllZones() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(null, null, null, null, null);
        Page<ZoneView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void search_withMerchantId_returnsMatchingZones() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria("merch-1", null, null, null, null);
        Page<ZoneView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(ZoneView::code)
                .containsExactlyInAnyOrder("ZON-1", "ZON-2");
    }

    @Test
    void search_withLocationId_returnsMatchingZones() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(null, "loc-uuid-2", null, null, null);
        Page<ZoneView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("ZON-3");
    }

    @Test
    void search_withQueryCodeMatch_returnsMatchingZones() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(null, null, "zon-1", null, null);
        Page<ZoneView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("ZON-1");
    }

    @Test
    void search_withQueryNameMatch_returnsMatchingZones() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(null, null, "storage", null, null);
        Page<ZoneView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(ZoneView::code)
                .containsExactlyInAnyOrder("ZON-2", "ZON-3");
    }

    @Test
    void search_withType_returnsMatchingZones() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(null, null, null, ZoneType.PICKING, null);
        Page<ZoneView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("ZON-1");
    }

    @Test
    void search_withActiveTrue_returnsActiveZones() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(null, null, null, null, true);
        Page<ZoneView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(ZoneView::code)
                .containsExactlyInAnyOrder("ZON-1", "ZON-2");
    }

    @Test
    void search_withPagination_respectsPageAndSize() {
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(null, null, null, null, null);
        Page<ZoneView> page0 = specification.search(criteria, PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "code")));
        Page<ZoneView> page1 = specification.search(criteria, PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "code")));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getContent()).extracting(ZoneView::code).containsExactly("ZON-1", "ZON-2");
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent()).extracting(ZoneView::code).containsExactly("ZON-3");
    }
}

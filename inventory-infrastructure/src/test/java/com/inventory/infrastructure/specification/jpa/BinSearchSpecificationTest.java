package com.inventory.infrastructure.specification.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.repository.jpa.BinJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.ZoneJpaRepository;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.BinView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BinSearchSpecificationTest extends RepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BinJpaRepository binJpaRepository;

    @Autowired
    private ZoneJpaRepository zoneJpaRepository;

    @Autowired
    private LocationJpaRepository locationJpaRepository;

    private BinSearchSpecification specification;

    private BinEntity activeBin1;
    private BinEntity activeBin2;
    private BinEntity inactiveBin;

    @BeforeEach
    void setUp() {
        binJpaRepository.deleteAll();
        zoneJpaRepository.deleteAll();
        locationJpaRepository.deleteAll();
        specification = new BinSearchSpecification(entityManager);

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

        ZoneEntity zone1 = new ZoneEntity();
        zone1.setUuid("zone-uuid-1");
        zone1.setCode("ZON-1");
        zone1.setName("Zone 1");
        zone1.setType(ZoneType.PICKING);
        zone1.setActive(true);
        zone1.setLocationId("loc-uuid-1");

        ZoneEntity zone2 = new ZoneEntity();
        zone2.setUuid("zone-uuid-2");
        zone2.setCode("ZON-2");
        zone2.setName("Zone 2");
        zone2.setType(ZoneType.STORAGE);
        zone2.setActive(true);
        zone2.setLocationId("loc-uuid-2");

        zoneJpaRepository.saveAll(List.of(zone1, zone2));

        activeBin1 = new BinEntity();
        activeBin1.setUuid("bin-uuid-1");
        activeBin1.setCode("BIN-1");
        activeBin1.setName("Bin One");
        activeBin1.setMaxCapacity(100);
        activeBin1.setActive(true);
        activeBin1.setZoneId("zone-uuid-1");

        activeBin2 = new BinEntity();
        activeBin2.setUuid("bin-uuid-2");
        activeBin2.setCode("BIN-2");
        activeBin2.setName("Bin Two");
        activeBin2.setMaxCapacity(100);
        activeBin2.setActive(true);
        activeBin2.setZoneId("zone-uuid-1");

        inactiveBin = new BinEntity();
        inactiveBin.setUuid("bin-uuid-3");
        inactiveBin.setCode("BIN-3");
        inactiveBin.setName("Bin Three");
        inactiveBin.setMaxCapacity(50);
        inactiveBin.setActive(false);
        inactiveBin.setZoneId("zone-uuid-2");

        binJpaRepository.saveAll(List.of(activeBin1, activeBin2, inactiveBin));
    }

    @Test
    void search_withEmptyCriteria_returnsAllBins() {
        BinSearchCriteria criteria = new BinSearchCriteria(null, null, null, null);
        Page<BinView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void search_withMerchantId_returnsMatchingBins() {
        BinSearchCriteria criteria = new BinSearchCriteria("merch-1", null, null, null);
        Page<BinView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(BinView::code)
                .containsExactlyInAnyOrder("BIN-1", "BIN-2");
    }

    @Test
    void search_withZoneId_returnsMatchingBins() {
        BinSearchCriteria criteria = new BinSearchCriteria(null, "zone-uuid-2", null, null);
        Page<BinView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("BIN-3");
    }

    @Test
    void search_withQueryCodeMatch_returnsMatchingBins() {
        BinSearchCriteria criteria = new BinSearchCriteria(null, null, "bin-1", null);
        Page<BinView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("BIN-1");
    }

    @Test
    void search_withQueryNameMatch_returnsMatchingBins() {
        BinSearchCriteria criteria = new BinSearchCriteria(null, null, "two", null);
        Page<BinView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("BIN-2");
    }

    @Test
    void search_withActiveTrue_returnsActiveBins() {
        BinSearchCriteria criteria = new BinSearchCriteria(null, null, null, true);
        Page<BinView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(BinView::code)
                .containsExactlyInAnyOrder("BIN-1", "BIN-2");
    }

    @Test
    void search_withPagination_respectsPageAndSize() {
        BinSearchCriteria criteria = new BinSearchCriteria(null, null, null, null);
        Page<BinView> page0 = specification.search(criteria, PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "code")));
        Page<BinView> page1 = specification.search(criteria, PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "code")));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getContent()).extracting(BinView::code).containsExactly("BIN-1", "BIN-2");
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent()).extracting(BinView::code).containsExactly("BIN-3");
    }
}

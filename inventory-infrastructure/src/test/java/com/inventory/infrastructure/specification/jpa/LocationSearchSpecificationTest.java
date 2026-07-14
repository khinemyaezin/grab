package com.inventory.infrastructure.specification.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.LocationView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationSearchSpecificationTest extends RepositoryTestConfig {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocationJpaRepository locationJpaRepository;

    private LocationSearchSpecification specification;

    private LocationEntity activeWarehouse;
    private LocationEntity activeStore;
    private LocationEntity inactiveWarehouse;

    @BeforeEach
    void setUp() {
        locationJpaRepository.deleteAll();
        specification = new LocationSearchSpecification(entityManager);

        activeWarehouse = new LocationEntity();
        activeWarehouse.setUuid("uuid-loc-1");
        activeWarehouse.setCode("LOC-1");
        activeWarehouse.setName("Warehouse One");
        activeWarehouse.setMerchantId("merch-1");
        activeWarehouse.setType(LocationType.WAREHOUSE);
        activeWarehouse.setActive(true);

        activeStore = new LocationEntity();
        activeStore.setUuid("uuid-loc-2");
        activeStore.setCode("LOC-2");
        activeStore.setName("Store Two");
        activeStore.setMerchantId("merch-1");
        activeStore.setType(LocationType.STORE);
        activeStore.setActive(true);

        inactiveWarehouse = new LocationEntity();
        inactiveWarehouse.setUuid("uuid-loc-3");
        inactiveWarehouse.setCode("LOC-3");
        inactiveWarehouse.setName("Warehouse Three");
        inactiveWarehouse.setMerchantId("merch-2");
        inactiveWarehouse.setType(LocationType.WAREHOUSE);
        inactiveWarehouse.setActive(false);

        locationJpaRepository.saveAll(List.of(activeWarehouse, activeStore, inactiveWarehouse));
    }

    @Test
    void search_withEmptyCriteria_returnsAllLocations() {
        LocationSearchCriteria criteria = new LocationSearchCriteria(null, null, null, null);
        Page<LocationView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void search_withMerchantId_returnsMatchingLocations() {
        LocationSearchCriteria criteria = new LocationSearchCriteria("merch-1", null, null, null);
        Page<LocationView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(LocationView::code)
                .containsExactlyInAnyOrder("LOC-1", "LOC-2");
    }

    @Test
    void search_withQueryCodeMatch_returnsMatchingLocations() {
        LocationSearchCriteria criteria = new LocationSearchCriteria(null, "loc-1", null, null);
        Page<LocationView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("LOC-1");
    }

    @Test
    void search_withQueryNameMatch_returnsMatchingLocations() {
        LocationSearchCriteria criteria = new LocationSearchCriteria(null, "ware", null, null);
        Page<LocationView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(LocationView::code)
                .containsExactlyInAnyOrder("LOC-1", "LOC-3");
    }

    @Test
    void search_withType_returnsMatchingLocations() {
        LocationSearchCriteria criteria = new LocationSearchCriteria(null, null, LocationType.STORE, null);
        Page<LocationView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("LOC-2");
    }

    @Test
    void search_withActiveTrue_returnsActiveLocations() {
        LocationSearchCriteria criteria = new LocationSearchCriteria(null, null, null, true);
        Page<LocationView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(LocationView::code)
                .containsExactlyInAnyOrder("LOC-1", "LOC-2");
    }

    @Test
    void search_withActiveFalse_returnsInactiveLocations() {
        LocationSearchCriteria criteria = new LocationSearchCriteria(null, null, null, false);
        Page<LocationView> result = specification.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("LOC-3");
    }

    @Test
    void search_withPagination_respectsPageAndSize() {
        LocationSearchCriteria criteria = new LocationSearchCriteria(null, null, null, null);
        Page<LocationView> page0 = specification.search(criteria, PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "code")));
        Page<LocationView> page1 = specification.search(criteria, PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "code")));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getContent()).extracting(LocationView::code).containsExactly("LOC-1", "LOC-2");
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page1.getContent()).extracting(LocationView::code).containsExactly("LOC-3");
    }
}

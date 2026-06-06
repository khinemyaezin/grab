package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.LocationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private LocationJpaRepository locationJpaRepository;

    @BeforeEach
    void setUp() {
        locationJpaRepository.deleteAll();

        LocationEntity activeWarehouse = new LocationEntity();
        activeWarehouse.setUuid("uuid-warehouse-1");
        activeWarehouse.setCode("WH-001");
        activeWarehouse.setName("Main Warehouse");
        activeWarehouse.setSellerId("seller-1");
        activeWarehouse.setType(LocationType.WAREHOUSE);
        activeWarehouse.setStreet("123 Main St");
        activeWarehouse.setCity("Springfield");
        activeWarehouse.setState("IL");
        activeWarehouse.setPostalCode("62701");
        activeWarehouse.setCountry("US");
        activeWarehouse.setActive(true);

        LocationEntity activeStore = new LocationEntity();
        activeStore.setUuid("uuid-store-1");
        activeStore.setCode("ST-001");
        activeStore.setName("Downtown Store");
        activeStore.setSellerId("seller-1");
        activeStore.setType(LocationType.STORE);
        activeStore.setStreet("456 Oak Ave");
        activeStore.setCity("Springfield");
        activeStore.setState("IL");
        activeStore.setPostalCode("62702");
        activeStore.setCountry("US");
        activeStore.setActive(true);

        LocationEntity inactiveWarehouse = new LocationEntity();
        inactiveWarehouse.setUuid("uuid-warehouse-2");
        inactiveWarehouse.setCode("WH-002");
        inactiveWarehouse.setName("Old Warehouse");
        inactiveWarehouse.setSellerId("seller-1");
        inactiveWarehouse.setType(LocationType.WAREHOUSE);
        inactiveWarehouse.setStreet("789 Elm St");
        inactiveWarehouse.setCity("Springfield");
        inactiveWarehouse.setState("IL");
        inactiveWarehouse.setPostalCode("62703");
        inactiveWarehouse.setCountry("US");
        inactiveWarehouse.setActive(false);

        locationJpaRepository.saveAll(java.util.List.of(activeWarehouse, activeStore, inactiveWarehouse));
    }

    @Test
    void findByUuid_returnsEntity_whenExists() {
        Optional<LocationEntity> result = locationJpaRepository.findByUuid("uuid-warehouse-1");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("WH-001");
        assertThat(result.get().getName()).isEqualTo("Main Warehouse");
    }

    @Test
    void findByUuid_returnsEmpty_whenNotExists() {
        Optional<LocationEntity> result = locationJpaRepository.findByUuid("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_returnsEntity_whenExists() {
        Optional<LocationEntity> result = locationJpaRepository.findByCode("ST-001");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("uuid-store-1");
        assertThat(result.get().getType()).isEqualTo(LocationType.STORE);
    }

    @Test
    void findByCode_returnsEmpty_whenNotExists() {
        Optional<LocationEntity> result = locationJpaRepository.findByCode("INVALID-CODE");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByCode_returnsTrue_whenExists() {
        boolean result = locationJpaRepository.existsByCode("WH-001");

        assertThat(result).isTrue();
    }

    @Test
    void existsByCode_returnsFalse_whenNotExists() {
        boolean result = locationJpaRepository.existsByCode("NON-EXISTENT");

        assertThat(result).isFalse();
    }

    @Test
    void findAllBySellerId_returnsAllLocationsForSeller() {
        Page<LocationView> result = locationJpaRepository.findAllBySellerId("seller-1", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(LocationView::code)
                .containsExactlyInAnyOrder("WH-001", "ST-001", "WH-002");
    }

    @Test
    void findAllBySellerId_returnsEmptyForUnknownSeller() {
        Page<LocationView> result = locationJpaRepository.findAllBySellerId("unknown-seller", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllBySellerIdAndActiveTrue_returnsOnlyActiveLocations() {
        Page<LocationView> result = locationJpaRepository.findAllBySellerIdAndActiveTrue("seller-1", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(LocationView::code)
                .containsExactlyInAnyOrder("WH-001", "ST-001");
        assertThat(result.getContent()).allMatch(LocationView::active);
    }

    @Test
    void findAllBySellerIdAndType_returnsOnlyMatchingType() {
        Page<LocationView> result = locationJpaRepository.findAllBySellerIdAndType("seller-1", LocationType.WAREHOUSE, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(LocationView::code)
                .containsExactlyInAnyOrder("WH-001", "WH-002");
        assertThat(result.getContent()).allMatch(v -> v.type() == LocationType.WAREHOUSE);
    }

    @Test
    void findAllBySellerIdAndType_returnsEmptyWhenNoMatch() {
        LocationEntity otherSellerStore = new LocationEntity();
        otherSellerStore.setUuid("uuid-other");
        otherSellerStore.setCode("ST-OTHER");
        otherSellerStore.setName("Other Store");
        otherSellerStore.setSellerId("seller-2");
        otherSellerStore.setType(LocationType.STORE);
        otherSellerStore.setActive(true);
        locationJpaRepository.save(otherSellerStore);

        Page<LocationView> result = locationJpaRepository.findAllBySellerIdAndType("seller-2", LocationType.WAREHOUSE, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllBySellerId_respectsPagination() {
        Page<LocationView> page0 = locationJpaRepository.findAllBySellerId("seller-1", PageRequest.of(0, 2));
        Page<LocationView> page1 = locationJpaRepository.findAllBySellerId("seller-1", PageRequest.of(1, 2));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page1.getContent()).hasSize(1);
    }
}

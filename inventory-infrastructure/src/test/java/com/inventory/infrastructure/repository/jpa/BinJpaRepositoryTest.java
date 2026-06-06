package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.BinView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class BinJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private BinJpaRepository binJpaRepository;

    private BinEntity activeBin1;
    private BinEntity activeBin2;
    private BinEntity inactiveBin;

    @BeforeEach
    void setUp() {
        binJpaRepository.deleteAll();

        activeBin1 = new BinEntity();
        activeBin1.setUuid("uuid-bin-1");
        activeBin1.setCode("BIN-A1");
        activeBin1.setName("Bin A1");
        activeBin1.setMaxCapacity(100);
        activeBin1.setActive(true);
        activeBin1.setZoneId("zone-1");

        activeBin2 = new BinEntity();
        activeBin2.setUuid("uuid-bin-2");
        activeBin2.setCode("BIN-A2");
        activeBin2.setName("Bin A2");
        activeBin2.setMaxCapacity(200);
        activeBin2.setActive(true);
        activeBin2.setZoneId("zone-1");

        inactiveBin = new BinEntity();
        inactiveBin.setUuid("uuid-bin-3");
        inactiveBin.setCode("BIN-B1");
        inactiveBin.setName("Bin B1");
        inactiveBin.setMaxCapacity(150);
        inactiveBin.setActive(false);
        inactiveBin.setZoneId("zone-1");

        binJpaRepository.saveAll(List.of(activeBin1, activeBin2, inactiveBin));
    }

    @Test
    void findByUuid_returnsEntity_whenExists() {
        Optional<BinEntity> result = binJpaRepository.findByUuid("uuid-bin-1");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("BIN-A1");
        assertThat(result.get().getName()).isEqualTo("Bin A1");
    }

    @Test
    void findByUuid_returnsEmpty_whenNotExists() {
        Optional<BinEntity> result = binJpaRepository.findByUuid("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByCodeAndZoneId_returnsEntity_whenExists() {
        Optional<BinEntity> result = binJpaRepository.findByCodeAndZoneId("BIN-A1", "zone-1");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("uuid-bin-1");
        assertThat(result.get().getMaxCapacity()).isEqualTo(100);
    }

    @Test
    void findByCodeAndZoneId_returnsEmpty_whenZoneMismatch() {
        Optional<BinEntity> result = binJpaRepository.findByCodeAndZoneId("BIN-A1", "zone-2");

        assertThat(result).isEmpty();
    }

    @Test
    void findByCodeAndZoneId_returnsEmpty_whenNotExists() {
        Optional<BinEntity> result = binJpaRepository.findByCodeAndZoneId("NON-EXISTENT", "zone-1");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByCodeAndZoneId_returnsTrue_whenExists() {
        boolean result = binJpaRepository.existsByCodeAndZoneId("BIN-A1", "zone-1");

        assertThat(result).isTrue();
    }

    @Test
    void existsByCodeAndZoneId_returnsFalse_whenNotExists() {
        boolean result = binJpaRepository.existsByCodeAndZoneId("BIN-A1", "zone-2");

        assertThat(result).isFalse();
    }

    @Test
    void findAllByZoneId_returnsAllBinsForZone() {
        Page<BinView> result = binJpaRepository.findAllByZoneId("zone-1", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(BinView::code)
                .containsExactlyInAnyOrder("BIN-A1", "BIN-A2", "BIN-B1");
    }

    @Test
    void findAllByZoneId_returnsEmptyForUnknownZone() {
        Page<BinView> result = binJpaRepository.findAllByZoneId("unknown-zone", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByZoneIdAndActive_returnsActiveBins() {
        Page<BinView> result = binJpaRepository.findAllByZoneIdAndActive("zone-1", true, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(BinView::code)
                .containsExactlyInAnyOrder("BIN-A1", "BIN-A2");
        assertThat(result.getContent()).allMatch(BinView::active);
    }

    @Test
    void findAllByZoneIdAndActive_returnsInactiveBins() {
        Page<BinView> result = binJpaRepository.findAllByZoneIdAndActive("zone-1", false, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("BIN-B1");
        assertThat(result.getContent()).noneMatch(BinView::active);
    }

    @Test
    void findAllByActive_returnsActiveBins() {
        Page<BinView> result = binJpaRepository.findAllByActive(true, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(BinView::active);
    }

    @Test
    void findAllByActive_returnsInactiveBins() {
        Page<BinView> result = binJpaRepository.findAllByActive(false, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).noneMatch(BinView::active);
    }

    @Test
    void findAllByZoneId_respectsPagination() {
        Page<BinView> page0 = binJpaRepository.findAllByZoneId("zone-1", PageRequest.of(0, 2));
        Page<BinView> page1 = binJpaRepository.findAllByZoneId("zone-1", PageRequest.of(1, 2));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page1.getContent()).hasSize(1);
    }
}

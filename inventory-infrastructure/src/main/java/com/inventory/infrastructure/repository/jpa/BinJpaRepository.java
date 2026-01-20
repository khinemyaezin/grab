package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.entity.BinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BinJpaRepository extends JpaRepository<BinEntity, Long> {

    Optional<BinEntity> findByUuid(String uuid);

    Optional<BinEntity> findByCodeAndZoneId(String code, Long zoneId);

    List<BinEntity> findAllByZoneId(Long zoneId);

    List<BinEntity> findAllByActiveTrue();

    boolean existsByCodeAndZoneId(String code, Long zoneId);
}

package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneJpaRepository extends JpaRepository<ZoneEntity, Long> {

    Optional<ZoneEntity> findByUuid(String uuid);

    Optional<ZoneEntity> findByCodeAndLocationId(String code, Long locationId);

    List<ZoneEntity> findAllByLocationId(Long locationId);

    List<ZoneEntity> findAllByType(ZoneType type);

    List<ZoneEntity> findAllByActiveTrue();

    boolean existsByCodeAndLocationId(String code, Long locationId);
}

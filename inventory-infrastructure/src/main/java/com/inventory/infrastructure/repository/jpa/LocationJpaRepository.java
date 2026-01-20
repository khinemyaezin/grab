package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationJpaRepository extends JpaRepository<LocationEntity, Long> {

    Optional<LocationEntity> findByUuid(String uuid);

    Optional<LocationEntity> findByCode(String code);

    List<LocationEntity> findAllByType(LocationType type);

    List<LocationEntity> findAllByActiveTrue();

    boolean existsByCode(String code);
}

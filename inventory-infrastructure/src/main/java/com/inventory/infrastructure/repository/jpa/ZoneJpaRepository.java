package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.view.ZoneView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneJpaRepository extends JpaRepository<ZoneEntity, Long> {

    Optional<ZoneEntity> findByUuid(String uuid);

    List<ZoneEntity> findAllByLocationIdAndActive(String locationId, boolean active);

    boolean existsByCodeAndLocationId(String code, String locationId);

    Page<ZoneView> findAllByLocationId(@Param("locationId") String locationId, Pageable pageable);

    Page<ZoneView> findAllByLocationIdAndActive(@Param("locationId") String locationId, @Param("active") boolean active, Pageable pageable);

    Page<ZoneView> findAllByType(@Param("type") ZoneType type, Pageable pageable);

    Page<ZoneView> findAllByActive(@Param("active") boolean active, Pageable pageable);
}

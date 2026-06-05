package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.view.BinView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BinJpaRepository extends JpaRepository<BinEntity, Long> {

    Optional<BinEntity> findByUuid(String uuid);

    Optional<BinEntity> findByCodeAndZoneId(String code, String zoneId);

    boolean existsByCodeAndZoneId(String code, String zoneId);

    Page<BinView> findAllByZoneId(String zoneId, Pageable pageable);

    Page<BinView> findAllByZoneIdAndActive(String zoneId, boolean active, Pageable pageable);

    Page<BinView> findAllByActive(boolean active, Pageable pageable);
}

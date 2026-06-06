package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.view.LocationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationJpaRepository extends JpaRepository<LocationEntity, Long> {

    Optional<LocationEntity> findByUuid(String uuid);

    Optional<LocationEntity> findByCode(String code);

    boolean existsByCode(String code);

    Page<LocationView> findAllBySellerId(@Param("sellerId") String sellerId, Pageable pageable);

    Page<LocationView> findAllBySellerIdAndActiveTrue(@Param("sellerId") String sellerId, Pageable pageable);

    Page<LocationView> findAllBySellerIdAndType(@Param("sellerId") String sellerId, @Param("type") LocationType type, Pageable pageable);
}

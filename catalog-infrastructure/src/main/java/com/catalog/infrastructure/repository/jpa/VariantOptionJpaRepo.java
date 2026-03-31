package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.VariantOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VariantOptionJpaRepo extends JpaRepository<VariantOptionEntity, Long> {
    Optional<VariantOptionEntity> findByUuid(String uuid);

    Optional<VariantOptionEntity> findByUuidAndVariantType_Uuid(String uuid, String typeUuid);

    List<VariantOptionEntity> findByVariantType_UuidOrderByNameAsc(String typeUuid);
}
